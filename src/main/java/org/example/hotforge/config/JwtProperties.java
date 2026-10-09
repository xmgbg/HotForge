package org.example.hotforge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")

public class JwtProperties {
    /**
     * JWT 签名密钥（Base64 编码）
     * — yml 中写的是 jwt.secret
     * — JwtUtil 启动时会拿它生成 HMAC-SHA 密钥对象
     * — 这个值绝不能泄露！生成方式：openssl rand -base64 32
     */
    private String secret;
    private long expiration;
}

