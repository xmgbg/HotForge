package org.example.hotforge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ================================
 * 登录/注册响应 DTO
 * ================================
 * 登录和注册成功后，返回 token + 用户基本信息。
 * 与 UserRespDTO 分离的原因：
 * — getCurrentUser 只返回用户信息，不需要 token
 * — register/login 需要同时返回 token + 用户信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRespDTO {

    /** JWT Token，前端存 localStorage，后续请求放 Authorization Header */
    private String token;

    /** 脱敏后的用户信息（不含密码） */
    private UserRespDTO user;
}