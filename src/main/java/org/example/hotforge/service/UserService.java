package org.example.hotforge.service;

import org.example.hotforge.dto.LoginReqDTO;
import org.example.hotforge.dto.LoginRespDTO;
import org.example.hotforge.dto.RegisterReqDTO;
import org.example.hotforge.dto.UpdateUserReqDTO;
import org.example.hotforge.dto.UserRespDTO;

/**
 * ================================
 * 用户业务接口
 * ================================
 */
public interface UserService {

    /**
     * 手机号注册
     * 自动分配 USER 角色，密码 BCrypt 加密入库，返回 token 实现注册即登录
     */
    LoginRespDTO register(RegisterReqDTO dto);

    /**
     * 手机号 + 密码登录
     * 校验账号状态（禁用则拒绝），验证通过后签发 JWT
     */
    LoginRespDTO login(LoginReqDTO dto);

    /**
     * 获取当前登录用户信息
     * @param userId 从 SecurityContext 取出的当前用户 ID，不用前端传，防越权
     */
    UserRespDTO getCurrentUser(Long userId);

    /**
     * 修改当前用户个人信息
     * 仅允许修改 nickname 和 avatar
     */
    void updateCurrentUser(Long userId, UpdateUserReqDTO dto);
}