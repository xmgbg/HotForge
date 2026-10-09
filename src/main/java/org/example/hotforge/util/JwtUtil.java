package org.example.hotforge.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hotforge.config.JwtProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {
    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_PHONE   = "phone";
    private static final String CLAIM_ROLE    = "role";
    private static final String CLAIM_TOKEN_VERSION = "tokenVersion";
    private final JwtProperties jwtProperties;

    /** Base64 解码后的 HMAC 签名密钥。 */
    private SecretKey secretKey;

    @jakarta.annotation.PostConstruct
    public void init() {
        String secret = jwtProperties.getSecret();
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT 密钥初始化完成");
    }

    /** 签发包含用户身份和令牌版本的 JWT。 */
    public String generateToken(Long userId, String phone, String role, Integer tokenVersion) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getExpiration() * 1000);

        String token = Jwts.builder()
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_PHONE, phone)
                .claim(CLAIM_ROLE, role)
                .claim(CLAIM_TOKEN_VERSION, tokenVersion)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(secretKey)
                .compact();

        log.debug("JWT 生成成功, userId={}, role={}", userId, role);
        return token;
    }
    /** 验证签名与有效期，返回令牌声明。 */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /** 从 Token 中提取用户 ID */
    public Long getUserId(String token) {
        return parseToken(token).get(CLAIM_USER_ID, Long.class);
    }

    /** 从 Token 中提取手机号 */
    public String getPhone(String token) {
        return parseToken(token).get(CLAIM_PHONE, String.class);
    }

    /** 从 Token 中提取角色 */
    public String getRole(String token) {
        return parseToken(token).get(CLAIM_ROLE, String.class);
    }
}
