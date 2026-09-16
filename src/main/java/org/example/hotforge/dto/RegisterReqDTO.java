package org.example.hotforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * ================================
 * 注册请求 DTO
 * ================================
 */
@Data
public class RegisterReqDTO {

    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度需在6-20位之间")
    private String password;

    /** 可选，不传则自动用手机号脱敏生成默认昵称 */
    private String nickname;
}