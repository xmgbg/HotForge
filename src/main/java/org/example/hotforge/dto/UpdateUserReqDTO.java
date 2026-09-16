package org.example.hotforge.dto;

import lombok.Data;

/**
 * ================================
 * 修改个人信息请求 DTO
 * ================================
 * 只允许改昵称和头像，手机号/密码/角色有独立的安全流程。
 */
@Data
public class UpdateUserReqDTO {

    private String nickname;

    private String avatar;
}