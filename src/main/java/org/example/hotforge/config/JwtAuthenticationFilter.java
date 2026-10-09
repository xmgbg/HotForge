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
import org.example.hotforge.mapper.UserMapper;
import org.example.hotforge.entity.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.security.SignatureException;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtUtil.parseToken(token);

            Long userId = claims.get("userId", Long.class);
            Integer tokenVersion = claims.get("tokenVersion", Integer.class);
            User currentUser = userId == null ? null : userMapper.selectById(userId);
            if (currentUser == null || !Integer.valueOf(1).equals(currentUser.getStatus())
                    || tokenVersion == null || !tokenVersion.equals(currentUser.getTokenVersion())) {
                filterChain.doFilter(request, response);
                return;
            }
            String role = currentUser.getRole();

            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

            UsernamePasswordAuthenticationToken authentication =
                    UsernamePasswordAuthenticationToken.authenticated(
                            userId, null, Collections.singletonList(authority));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("JWT 认证成功, userId={}, role={}, uri={}", userId, role,
                    request.getRequestURI());

        } catch (ExpiredJwtException e) {
            log.warn("JWT 已过期, uri={}", request.getRequestURI());
        } catch (MalformedJwtException | SignatureException e) {
            log.error("JWT 非法, uri={}, msg={}", request.getRequestURI(), e.getMessage());
        } catch (Exception e) {
            log.error("JWT 解析异常, uri={}", request.getRequestURI(), e);
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
