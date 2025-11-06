package com.seu.airline.service.impl;

import com.seu.airline.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁服务
 * 用于避免航班超售等并发场景下的数据一致性问题
 */
@Slf4j
@Service
public class RedisLockService {

    @Autowired
    private RedisService redisService;

    // 锁的默认过期时间（秒）
    private static final long DEFAULT_EXPIRE_TIME = 30;
    
    // 锁的重试间隔（毫秒）
    private static final long RETRY_INTERVAL = 100;
    
    // 锁的前缀
    private static final String LOCK_PREFIX = "flight:lock:seat:";

    /**
     * 尝试获取分布式锁
     * @param seatId 座位ID
     * @param timeout 尝试获取锁的超时时间（毫秒）
     * @return 锁标识，如果获取失败返回null
     */
    public String tryLock(Long seatId, long timeout) {
        String lockKey = LOCK_PREFIX + seatId;
        String lockValue = UUID.randomUUID().toString();
        
        long startTime = System.currentTimeMillis();
        
        while (System.currentTimeMillis() - startTime < timeout) {
            // 使用setIfAbsent实现分布式锁（原子操作）
            Boolean success = redisService.redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, DEFAULT_EXPIRE_TIME, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(success)) {
                log.info("成功获取座位锁: seatId={}, lockValue={}", seatId, lockValue);
                return lockValue;
            }
            
            // 短暂休眠后重试
            try {
                Thread.sleep(RETRY_INTERVAL);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        log.warn("获取座位锁超时: seatId={}, timeout={}ms", seatId, timeout);
        return null;
    }

    /**
     * 释放分布式锁
     * @param seatId 座位ID
     * @param lockValue 锁标识
     * @return 是否释放成功
     */
    public boolean unlock(Long seatId, String lockValue) {
        if (lockValue == null) {
            return false;
        }
        
        String lockKey = LOCK_PREFIX + seatId;
        try {
            // 使用Lua脚本原子性地获取锁值并删除，避免并发问题
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Object result = redisService.redisTemplate.execute(
                (RedisConnection connection) -> {
                    RedisScript<Long> redisScript = RedisScript.of(script, Long.class);
                    return redisService.redisTemplate.execute(redisScript, Collections.singletonList(lockKey), lockValue);
                }
            );
           
            boolean success = result != null && Long.valueOf(1).equals(result);
            if (success) {
                log.info("成功释放座位锁: seatId={}, lockValue={}", seatId, lockValue);
            }
            return success;
        } catch (Exception e) {
            log.error("释放座位锁失败: seatId={}", seatId, e);
            // 为了避免死锁，即使出现异常也返回true表示已尝试释放
            return true;
        } 
    }

    /**
     * 尝试获取锁并执行操作
     * @param seatId 座位ID
     * @param timeout 尝试获取锁的超时时间（毫秒）
     * @param action 要执行的操作
     * @param <T> 返回类型
     * @return 操作结果
     * @throws Exception 如果获取锁失败或操作执行异常
     */
    public <T> T executeWithLock(Long seatId, long timeout, LockAction<T> action) throws Exception {
        String lockValue = null;
        
        try {
            // 尝试获取锁
            lockValue = tryLock(seatId, timeout);
            if (lockValue == null) {
                String errorMsg = String.format("获取座位锁失败，可能存在并发操作: seatId=%d, timeout=%dms", seatId, timeout);
                log.warn(errorMsg);
                throw new RuntimeException(errorMsg);
            }
            
            log.debug("执行座位锁保护的操作: seatId={}", seatId);
            // 执行操作
            return action.execute();
        } catch (Exception e) {
            // 记录执行过程中的异常
            log.error("执行座位锁保护的操作失败: seatId={}, error={}", seatId, e.getMessage(), e);
            throw e;
        } finally {
            // 释放锁
            if (lockValue != null) {
                boolean unlockSuccess = unlock(seatId, lockValue);
                if (!unlockSuccess) {
                    log.warn("释放座位锁可能失败，需要检查锁状态: seatId={}", seatId);
                }
            }
        }
    }

    /**
     * 锁操作接口
     */
    @FunctionalInterface
    public interface LockAction<T> {
        T execute() throws Exception;
    }
}