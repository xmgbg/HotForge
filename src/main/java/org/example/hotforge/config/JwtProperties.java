package org.example.hotforge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ================================
 * JWT 配置属性类
 * ================================
 * 是什么：把 application.yml 中以 "jwt" 开头的配置项，
 *        自动映射到这个类的同名字段上。
 *
 * 为什么：
 * 1. 比 @Value 更安全——字段名写错编译期就能发现
 * 2. 集中管理——所有 JWT 相关配置只在这一个类里
 * 3. IDE 友好——在 yml 里写 "jwt." 时能自动补全
 *
 * 数据流：
 * application.yml  →  JwtProperties  →  JwtUtil（用它来签发/解析 Token）
 *
 * 对应 yml 中的：
 * jwt:
 *   secret: ${JWT_SECRET}
 *   expiration: 604800
 */
@Data                                           // Lombok：自动生成 getter/setter
@Component                                      // 交给 Spring 管理（JwtUtil 要注入它）
@ConfigurationProperties(prefix = "jwt")        // 绑定 yml 中 jwt.* 的所有属性

public class JwtProperties {
    /**
     * JWT 签名密钥（Base64 编码）
     * — yml 中写的是 jwt.secret
     * — JwtUtil 启动时会拿它生成 HMAC-SHA 密钥对象
     * — 这个值绝不能泄露！生成方式：openssl rand -base64 32
     */
    private String secret;
    /**
     * Token 有效期，单位：秒
     * — yml 中写的是 jwt.expiration
     * — 604800 秒 = 7 天（BR-14）
     * — JwtUtil 生成 Token 时：new Date(now + expiration * 1000)
     */
    private long expiration;
}

