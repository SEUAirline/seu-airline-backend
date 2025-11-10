package com.seu.airline.controller;

import com.seu.airline.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/public/test/encoding")
public class EncodingTestController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 测试创建临时表并插入中文数据
    @PostMapping("/test-insert")
    public ApiResponse<String> testInsertChinese() {
        try {
            // 创建临时表（如果不存在）
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS encoding_test (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "chinese_text VARCHAR(100) NOT NULL, " +
                "description VARCHAR(255) " +
                ") ENGINE = InnoDB DEFAULT CHARSET = utf8mb4");

            // 插入中文测试数据
            String testText = "测试中文编码插入，这是中文内容123！@#";
            jdbcTemplate.update(
                "INSERT INTO encoding_test (chinese_text, description) VALUES (?, ?)",
                testText,
                "这是一个中文编码测试描述"
            );

            return ApiResponse.success("插入内容: " + testText, "中文数据插入成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage(), "中文数据插入失败");
        }
    }

    // 测试读取中文数据
    @GetMapping("/test-read")
    public ApiResponse<List<Map<String, Object>>> testReadChinese() {
        try {
            // 查询最近插入的中文数据
            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                "SELECT * FROM encoding_test ORDER BY id DESC LIMIT 10"
            );
            return ApiResponse.success(results, "中文数据读取成功");
        } catch (Exception e) {
            return ApiResponse.error(null, "中文数据读取失败: " + e.getMessage());
        }
    }

    // 测试插入包含特殊字符的中文数据
    @PostMapping("/test-special-chars")
    public ApiResponse<String> testSpecialChars() {
        try {
            String specialText = "特殊字符测试：中文标点符号，。！？；：\"\"''《》、" +
                "\"\"''【】{}（）-+*/=<>@#$%^&*()_+" +
                "生僻字：龘靐齉齾龗龖鱻爩麤灪爨癵籱麣纞钃鸜麷鞻韽韾顟顠饙饙騳騱";
            
            jdbcTemplate.update(
                "INSERT INTO encoding_test (chinese_text, description) VALUES (?, ?)",
                specialText,
                "特殊字符和生僻字测试"
            );
            
            return ApiResponse.success("插入内容: " + specialText, "特殊字符中文数据插入成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage(), "特殊字符中文数据插入失败");
        }
    }

    // 清理测试数据
    @DeleteMapping("/cleanup")
    public ApiResponse<String> cleanup() {
        try {
            jdbcTemplate.execute("DROP TABLE IF EXISTS encoding_test");
            return ApiResponse.success("清理完成", "测试表已删除");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage(), "清理失败");
        }
    }
}