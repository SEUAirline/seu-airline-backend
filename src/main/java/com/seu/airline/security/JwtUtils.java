package com.seu.airline.security;

import com.seu.airline.service.RedisService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    // Redis key前缀
    private static final String TOKEN_PREFIX = "jwt:token:";
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    @Autowired
    private RedisService redisService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    private Key key;

    @PostConstruct
    public void init() {
        // 确保使用配置文件中的固定密钥，并满足HS512算法要求
        // 正确的做法是直接使用配置的密钥，前提是它足够长（至少64字符）
        // 这里我们使用Keys.hmacShaKeyFor方法处理，它会确保密钥安全
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        // 重要：在实际生产环境中，确保jwt.secret配置值是至少64字符长的随机字符串
        // 并且所有应用实例使用完全相同的密钥配置
        logger.info("JWT密钥已从配置文件初始化");
    }

    // 生成JWT token
    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        String username = userPrincipal.getUsername();

        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(this.key, SignatureAlgorithm.HS512)
                .compact();

        // 将token存储到Redis中，设置过期时间
        String redisKey = TOKEN_PREFIX + username;
        redisService.set(redisKey, token, jwtExpirationMs, TimeUnit.MILLISECONDS);

        logger.debug("Token generated and stored in Redis for user: {}", username);
        return token;
    }

    // 从token中获取用户名
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(this.key)
                .build()
                .parseClaimsJws(token)
                .getBody().getSubject();
    }

    // 验证JWT token
    public boolean validateJwtToken(String authToken) {
        try {
            // 首先检查token是否在黑名单中
            if (isTokenBlacklisted(authToken)) {
                logger.warn("Token is in blacklist");
                return false;
            }

            // 验证token的签名和有效性
            Jwts.parserBuilder()
                    .setSigningKey(this.key)
                    .build()
                    .parseClaimsJws(authToken);

            // 验证token是否在Redis中存在
            String username = getUserNameFromJwtToken(authToken);
            String redisKey = TOKEN_PREFIX + username;
            Object storedToken = redisService.get(redisKey);

            if (storedToken == null) {
                logger.warn("Token not found in Redis for user: {}", username);
                return false;
            }

            // 验证Redis中的token和当前token是否一致（防止旧token使用）
            if (!authToken.equals(storedToken.toString())) {
                logger.warn("Token mismatch for user: {}", username);
                return false;
            }

            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 将token加入黑名单
     * 
     * @param token JWT token
     */
    public void addTokenToBlacklist(String token) {
        try {
            String username = getUserNameFromJwtToken(token);
            String blacklistKey = BLACKLIST_PREFIX + token;

            // 获取token的剩余有效时间
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(this.key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Date expiration = claims.getExpiration();
            long remainingTime = expiration.getTime() - System.currentTimeMillis();

            if (remainingTime > 0) {
                // 将token加入黑名单，设置过期时间为token的剩余有效时间
                redisService.set(blacklistKey, username, remainingTime, TimeUnit.MILLISECONDS);

                // 同时从Redis中删除有效token
                String tokenKey = TOKEN_PREFIX + username;
                redisService.delete(tokenKey);

                logger.info("Token added to blacklist for user: {}", username);
            }
        } catch (Exception e) {
            logger.error("Error adding token to blacklist: {}", e.getMessage());
        }
    }

    /**
     * 检查token是否在黑名单中
     * 
     * @param token JWT token
     * @return 是否在黑名单中
     */
    public boolean isTokenBlacklisted(String token) {
        String blacklistKey = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisService.hasKey(blacklistKey));
    }

    /**
     * 刷新token
     * 
     * @param oldToken 旧的token
     * @return 新的token
     */
    public String refreshToken(String oldToken) {
        try {
            String username = getUserNameFromJwtToken(oldToken);

            // 生成新token
            String newToken = Jwts.builder()
                    .setSubject(username)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                    .signWith(this.key, SignatureAlgorithm.HS512)
                    .compact();

            // 将旧token加入黑名单
            addTokenToBlacklist(oldToken);

            // 将新token存储到Redis
            String redisKey = TOKEN_PREFIX + username;
            redisService.set(redisKey, newToken, jwtExpirationMs, TimeUnit.MILLISECONDS);

            logger.info("Token refreshed for user: {}", username);
            return newToken;
        } catch (Exception e) {
            logger.error("Error refreshing token: {}", e.getMessage());
            return null;
        }
    }
}