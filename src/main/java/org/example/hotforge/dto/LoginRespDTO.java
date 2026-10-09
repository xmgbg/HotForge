package org.example.hotforge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
