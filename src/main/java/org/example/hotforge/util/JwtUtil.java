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

/**
 * ================================
 * JWT 工具类
 * ================================
 * 是什么：把 jjwt 库的底层 API 封装成项目可直接用的三个能力——
 *       ① 生成 Token（登录成功时调用）
 *       ② 解析 Token（Filter 里用，拿用户信息）
 *       ③ 提取 Token 中的字段（快捷方法）
 *
 * 为什么这样设计：
 * 1. 集中管理 JWT 逻辑——将来换库、换算法、加字段，只改这一个类
 * 2. SecretKey 在 @PostConstruct 里一次性初始化，不用每次签名都重新构造
 * 3. parseToken 不吞异常——让调用方（Filter）决定怎么处理过期/篡改/格式错误
 *
 * JWT 结构（三段式，Base64 编码，点号分隔）：
 * Header . Payload . Signature
 *   Header  : {"alg":"HS256"}                   → 算法声明
 *   Payload : {"userId":1,"phone":"138...","role":"USER","iat":...,"exp":...}用户信息 + 签发时间 + 过期时间
 *   Signature: HMAC-SHA256(Header + "." + Payload, secret)  → 防篡改 + 验证用户身份
 */
@Slf4j                                          // 可以直接用 log 对象
@Component                                      // 让 Spring 管理，哪里需要就 @Autowired
@RequiredArgsConstructor                        // 只对 final 字段生成构造器，Spring自动注入

public class JwtUtil {
    // ==========================================
    // 声明在 JWT Payload 中的自定义字段名（常量防手误）
    // ==========================================
    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_PHONE   = "phone";
    private static final String CLAIM_ROLE    = "role";
    private static final String CLAIM_TOKEN_VERSION = "tokenVersion";
    // ==========================================
    // 依赖注入
    // ==========================================
    private final JwtProperties jwtProperties;  // final + @RequiredArgsConstructor =构造器注入

    /**
     * 签名密钥对象
     * — 在 @PostConstruct 中一次性初始化，之后只读不写
     * — HMAC-SHA256 要求密钥 ≥ 256 位（32 字节），我们的 secret 是 Base64 编码的 32
     字节随机串
     */
    private SecretKey secretKey;

    // ==========================================
    // 初始化：把 yml 里的 Base64 字符串转成密钥对象
    // ==========================================
    /**
     * @PostConstruct：构造器执行完 + 依赖注入完成后，Spring 自动调用一次
     * 为什么放这里而不是构造器？
     * — 构造器执行时 Spring 还没完成注入，jwtProperties 可能是 null
     * — @PostConstruct 保证所有依赖都到位了再跑
     */
    @jakarta.annotation.PostConstruct
    public void init() {
        // 1. 从 yml 拿到 Base64 编码的密钥字符串
        String secret = jwtProperties.getSecret();
        // 2. Base64 解码 → 字节数组
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        // 3. 用字节数组构造 HMAC-SHA 密钥对象
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT 密钥初始化完成");
    }

    // ==========================================
    // 1. 生成 Token
    // ==========================================
    /**
     * 什么时候调用：用户登录成功、手机号密码验证通过后
     *
     * 做了什么：
     * ① 把 userId、phone、role 塞进 Payload（自定义 claims）
     * ② 设置签发时间（iat）和过期时间（exp）
     * ③ 用密钥签名，返回最终的 Token 字符串
     *
     * @param userId 用户 ID（主键）
     * @param phone  手机号
     * @param role   角色（USER / VIP / TRAINER / ADMIN）
     * @return JWT Token 字符串（可直接放入 HTTP Header）
     */
    public String generateToken(Long userId, String phone, String role, Integer tokenVersion) {
        // 当前时间 → 用来算 iat（签发时间）和 exp（过期时间）
        Date now = new Date();
        // 过期时间 = 当前时间 + yml 配置的有效期（秒→毫秒需 ×1000）
        Date expiration = new Date(now.getTime() + jwtProperties.getExpiration() * 1000);

        // Jwts.builder() 是 jjwt 提供的建造器模式
        String token = Jwts.builder()
                // ── 自定义 claims（Payload 中的业务字段）──
                .claim(CLAIM_USER_ID, userId)       // "userId" → 用户 ID
                .claim(CLAIM_PHONE, phone)          // "phone"  → 手机号
                .claim(CLAIM_ROLE, role)            // "role"   → 角色
                .claim(CLAIM_TOKEN_VERSION, tokenVersion)
                // ── 标准 claims ──
                .setIssuedAt(now)                   // iat：签发时间
                .setExpiration(expiration)          // exp：过期时间
                // ── 签名 ──
                .signWith(secretKey)                // 用 HMAC-SHA256 签名
                .compact();                         // 最终输出：一段 Base64 字符串

        log.debug("JWT 生成成功, userId={}, role={}", userId, role);
        return token;
    }
    // ==========================================
    // 2. 解析 Token（核心方法，会抛异常）
    // ==========================================
    /**
     * 是什么：把 Token 字符串还原成 Claims 对象，同时验证签名和有效期
     *
     * 为什么不吞异常？
     * — 调用方（JwtAuthenticationFilter）需要区分过期/篡改/格式错误
     * — Token 过期只是 warn（正常行为），签名不对是 error（可能是攻击）
     * — 吞掉了调用方就没法区分了
     *
     * @param token JWT Token 字符串
     * @return Claims 对象（包含 userId、phone、role 等）
     * @throws ExpiredJwtException       Token 已过期
     * @throws MalformedJwtException     Token 格式不对（拼错/截断）
     * @throws SignatureException        签名验证失败（被篡改）
     * @throws IllegalArgumentException  Token 为空
     */
    public Claims parseToken(String token) {
        // Jwts.parserBuilder() → 设置签名密钥 → parseClaimsJws() 解析
        // parseClaimsJws 会做三件事：
        //   ① 验证签名是否匹配（防篡改）
        //   ② 检查 exp 是否过期
        //   ③ 把 Payload 解析成 Claims 对象
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)               // 告诉 jjwt 用什么密钥验证签名
                .build()                                // 构造解析器
                .parseClaimsJws(token)                  // 解析 + 验证
                .getBody();                             // 拿到 Payload（Claims）
    }

    // ==========================================
    // 3. 快捷取值方法（内部都复用 parseToken）
    // ==========================================
    /**
     * 实际使用时，Filter 里只 parseToken 一次拿到 Claims，
     * 然后直接从 Claims 里取值（避免重复解析校验）。
     * 这三个方法作为便利入口，适合简单场景直接调。
     */

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
