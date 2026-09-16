package org.example.hotforge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ================================
 * 用户信息响应 DTO
 * ================================
 * 返回给前端的用户信息。不含 password 字段，安全脱敏。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRespDTO {

    private Long id;
    private String phone;
    private String nickname;
    private String avatar;
    private String role;
    private LocalDateTime vipExpireTime;
    private Integer status;
    private LocalDateTime createdAt;
}