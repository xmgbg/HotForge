package org.example.hotforge.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * ================================
 * 登录请求 DTO
 * ================================
 */
@Data
public class LoginReqDTO {

    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "密码不能为空")
    private String password;
}