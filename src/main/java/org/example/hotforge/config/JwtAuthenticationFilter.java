package org.example.hotforge.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hotforge.util.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.security.SignatureException;

import java.io.IOException;
import java.util.Collections;

/**
 * ================================
 * JWT 认证过滤器
 * ================================
 * 是什么：每个 HTTP 请求到达 Controller 之前，先经过这个过滤器。
 *        从 Header 里拿到 Token → 解析出用户信息 → 注入 SecurityContext。
 *
 * 为什么继承 OncePerRequestFilter？
 * — 保证一个请求只过滤一次（Tomcat 内部可能 forward/include，普通 Filter 会重复执行）
 *
 * 数据流：
 * 请求 → JwtAuthenticationFilter（本类）→ SecurityConfig 鉴权 → Controller
 *        ├─ 有 Token + 合法：注入 SecurityContext，Controller 能拿到当前用户
 *        ├─ 有 Token + 过期/非法：记日志，清空上下文，SecurityConfig 返回 401
 *        └─ 无 Token：直接放行，SecurityConfig 返回 401
 */
@Slf4j
@Component
@RequiredArgsConstructor// 依赖注入 JwtUtil
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // ==========================================
    // 依赖注入
    // ==========================================
    private final JwtUtil jwtUtil;

    // ==========================================
    // 核心过滤逻辑
    // ==========================================
    /**
     * 每个请求都会进这个方法。
     *
     * 处理流程：
     * ① 从 Header "Authorization: Bearer xxx" 中提取 Token 字符串
     * ② 没 Token → 直接放行（交给 SecurityConfig 的 .anyRequest().authenticated() 拦截）
     * ③ 解析 Token，拿到 userId + phone + role
     * ④ 构造 UsernamePasswordAuthenticationToken 并注入 SecurityContextHolder
     * ⑤ 解析失败（过期/篡改/格式错）→ 记日志，不注入上下文
     * ⑥ 无论成败都放行（返回 401/403 的活交给 SecurityConfig 的 exceptionHandling）
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // ── ① 从 Header 提取 Token ──
        String token = extractToken(request);

        // ── ② 没 Token → 这是匿名请求（登录/注册 or 未登录访问受保护接口）──
        if (!StringUtils.hasText(token)) {
            // 不阻断，放给后面的 SecurityConfig 处理：
            //   白名单路径 → 放行
            //   受保护路径 → 返回 401
            filterChain.doFilter(request, response);
            return;
        }

        // ── ③ 解析 Token 并注入 SecurityContext ──
        try {
            // 解析 Token（内部会验证签名 + 有效期，任一失败就抛异常）
            Claims claims = jwtUtil.parseToken(token);

            // 从 Payload 中拿出三个关键字段
            Long userId = claims.get("userId", Long.class);
            String phone = claims.get("phone", String.class);
            String role   = claims.get("role", String.class);

            // 构造 Spring Security 的权限对象
            // "ROLE_" 前缀是 Spring Security 的约定（hasRole("ADMIN") 实际"ROLE_ADMIN"）
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

            // 构造已认证令牌
            // 参数：principal=userId（主体标识），credentials=null（密码不需要了），authorities=权限列表
            UsernamePasswordAuthenticationToken authentication =
                    UsernamePasswordAuthenticationToken.authenticated(
                            userId, null, Collections.singletonList(authority));

            // 把认证信息存入当前线程的安全上下文
            // 之后 Controller 里通过 SecurityContextHolder.getContext().getAuthentication()就能拿到
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("JWT 认证成功, userId={}, role={}, uri={}", userId, role,
                    request.getRequestURI());

        } catch (ExpiredJwtException e) {
            // Token 过期 — 正常行为（7 天到期），记 warn 即可
            log.warn("JWT 已过期, uri={}", request.getRequestURI());
        } catch (MalformedJwtException | SignatureException e) {
            // 格式不对 or 签名验证失败 — 可能是恶意请求，记 error
            log.error("JWT 非法, uri={}, msg={}", request.getRequestURI(), e.getMessage());
        } catch (Exception e) {
            // 其他未预期的解析异常，记 error 含堆栈方便排查
            log.error("JWT 解析异常, uri={}", request.getRequestURI(), e);
        }

        // ── ⑥ 无论解析成功还是失败，都放行 ──
        // 原因：解析失败我们也不在这里直接写 401 响应，
        //      交给 SecurityConfig 的 exceptionHandling 统一处理响应格式
        filterChain.doFilter(request, response);
    }

    // ==========================================
    // 从请求头中提取 Token
    // ==========================================
    /**
     * 标准格式：Authorization: Bearer <token>
     *
     * 做了什么：
     * ① 取 Authorization 头的值
     * ② 判断是否以 "Bearer " 开头（忽略大小写问题，实际标准首字母大写）
     * ③ 截掉 "Bearer " 前缀，返回纯 Token
     * ④ 没带 or 格式不对 → 返回 null
     */
    private String extractToken(HttpServletRequest request) {
        // getHeader 拿的是请求头的原始字符串
        String bearerToken = request.getHeader("Authorization");

        // 两个条件：不为空 + 以 "Bearer " 开头
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // substring(7)：跳过 "Bearer " 这 7 个字符
            return bearerToken.substring(7);
        }

        return null;
    }
}
