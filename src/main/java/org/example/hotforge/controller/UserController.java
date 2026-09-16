package org.example.hotforge.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.hotforge.common.result.Result;
import org.example.hotforge.dto.LoginReqDTO;
import org.example.hotforge.dto.LoginRespDTO;
import org.example.hotforge.dto.RegisterReqDTO;
import org.example.hotforge.dto.UpdateUserReqDTO;
import org.example.hotforge.dto.UserRespDTO;
import org.example.hotforge.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ================================
 * 用户 Controller
 * ================================
 * 接收前端请求 → 参数校验 → 调用 Service → 统一返回 Result<T>
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ==========================================
    // 注册（白名单，无需登录）
    // ==========================================
    @PostMapping("/register")
    public Result<LoginRespDTO> register(@Valid @RequestBody RegisterReqDTO dto) {
        LoginRespDTO result = userService.register(dto);
        return Result.success("注册成功", result);
    }

    // ==========================================
    // 登录（白名单，无需登录）
    // ==========================================
    @PostMapping("/login")
    public Result<LoginRespDTO> login(@Valid @RequestBody LoginReqDTO dto) {
        LoginRespDTO result = userService.login(dto);
        return Result.success("登录成功", result);
    }

    // ==========================================
    // 获取当前用户信息（需登录）
    // ==========================================
    @GetMapping("/me")
    public Result<UserRespDTO> getCurrentUser() {
        Long userId = getCurrentUserId();
        UserRespDTO user = userService.getCurrentUser(userId);
        return Result.success(user);
    }

    // ==========================================
    // 修改个人信息（需登录）
    // ==========================================
    @PutMapping("/me")
    public Result<Void> updateCurrentUser(@Valid @RequestBody UpdateUserReqDTO dto) {
        Long userId = getCurrentUserId();
        userService.updateCurrentUser(userId, dto);
        return Result.success("修改成功");
    }

    // ==========================================
    // 内部工具方法
    // ==========================================

    /**
     * 从 SecurityContext 中取出当前登录用户的 ID。
     * JwtAuthenticationFilter 在请求进来时已经把 userId 设入了 Authentication，
     * 这里直接取用，不需前端传 userId，天然防越权。
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }
}