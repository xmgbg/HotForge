package org.example.hotforge.config;

import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.hotforge.common.result.Result;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * ================================
 * Spring Security 总配置
 * ================================
 * 是什么：整个项目的安全策略都在这一个类里定义——
 *       ① 哪些路径放行（登录/注册）
 *       ② JWT 过滤器插在哪个位置
 *       ③ 认证失败 / 权限不足返回什么
 *       ④ 密码用什么算法加密
 *
 * 为什么这样设计：
 * 1. 前后端分离 → 禁用 CSRF（不靠 Cookie，不存在跨站伪造风险）
 * 2. JWT 认证 → 无状态会话（STATELESS），服务器不存 Session
 * 3. @EnableMethodSecurity → 可以在 Controller 方法上用 @PreAuthorize("hasRole('ADMIN')")
 * 4. 认证失败返回 JSON → 前后端分离不能重定向到登录页，前端只认 JSON
 *
 * Spring Security 过滤器链执行顺序（与我们相关的）：
 * 请求 → ... → JwtAuthenticationFilter → ... → ExceptionTranslationFilter → ... →
 Controller
 *                                                ↑
 *                          认证失败/鉴权失败在这里被拦截，转成 JSON 响应
 */
@Configuration                                  // 标记这是一个配置类
@EnableWebSecurity                              // 开启 Spring Security 的 Web 安全支持
@EnableMethodSecurity                           // 开启方法级权限注解（@PreAuthorize 等）
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==========================================
    // 安全过滤链（最核心）
    // ==========================================
    /**
     * 返回一个 SecurityFilterChain Bean，Spring Security 会自动识别并装配。
     *
     * 这个方法的入参 HttpSecurity 由 Spring 自动注入，
     * 我们通过链式调用配置它，最后 build() 返回过滤链。
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ── ① 禁用 CSRF ──
                // 为什么：前后端分离，不依赖 Cookie 传递会话凭证，
                //        不存在 CSRF（跨站请求伪造）的攻击条件
                .csrf(AbstractHttpConfigurer::disable)

                // ── ② 无状态会话 ──
                // 为什么：JWT 本身就携带用户信息，服务端不需要 Session 来记住登录状态
                //        STATELESS = Spring Security 绝不创建/使用 HttpSession
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ── ③ 路径权限规则 ──
                .authorizeHttpRequests(auth -> auth
                        // 白名单：登录和注册接口不拦截
                        .requestMatchers("/api/user/login",
                                "/api/user/register",
                                "/api/auth/verification-codes",
                                "/api/auth/password/reset").permitAll()
                        // 其他所有请求都要认证
                        .anyRequest().authenticated()
                )

        // ── ④ 把 JWT 过滤器插在 UsernamePasswordAuthenticationFilter 之前 ──
        // UsernamePasswordAuthenticationFilter 是 Spring Security默认的表单登录过滤器，
        // 我们不用表单登录，但需要 JWT Filter在它之前执行（在认证决策之前注入用户信息）
                  .addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class)

                // ── ⑤ 认证失败 / 鉴权失败 → 统一返回 JSON ──
                .exceptionHandling(ex -> ex
                        // 认证失败：没登录、Token 过期、Token 非法
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            Result<Void> result = Result.unauthorized();
                            response.getWriter().write(objectMapper.writeValueAsString(result));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            Result<Void> result = Result.forbidden();
                            response.getWriter().write(objectMapper.writeValueAsString(result));
                        })
                );

        // build() 把以上所有配置"编译"成一条 SecurityFilterChain 实例
        return http.build();
    }

    // ==========================================
    // 密码编码器
    // ==========================================
    /**
     * 暴露一个 BCryptPasswordEncoder Bean，整个项目共用。
     *
     * BCrypt 是什么：
     * — 一种专为密码存储设计的哈希算法，自带盐值（salt）
     * — 相同密码每次加密结果不同（因为随机盐），天然防彩虹表
     * — 计算速度故意慢（可配置 cost 参数），增加暴力破解成本
     *
     * 使用场景：
     * — 注册时：passwordEncoder.encode(rawPassword) → 存入数据库
     * — 登录时：passwordEncoder.matches(rawPassword, encodedPassword) → 比对
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 默认 cost = 10，即 2^10 = 1024 轮哈希迭代
        return new BCryptPasswordEncoder();
    }

    /**
     * JwtAuthenticationFilter 只应存在于 Spring Security 过滤链中。
     * 禁止 Servlet 容器再次自动注册，避免过滤器重复执行并覆盖认证上下文。
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(
            JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration =
                new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
