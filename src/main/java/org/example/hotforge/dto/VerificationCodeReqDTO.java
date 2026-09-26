package org.example.hotforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerificationCodeReqDTO {
    @NotBlank
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank
    @Pattern(regexp = "REGISTER|RESET_PASSWORD", message = "验证码用途不正确")
    private String purpose;
}
