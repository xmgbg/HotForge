package org.example.hotforge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private String trainerStatus;
    private LocalDateTime vipExpireTime;
    private Integer status;
    private LocalDateTime createdAt;
}
