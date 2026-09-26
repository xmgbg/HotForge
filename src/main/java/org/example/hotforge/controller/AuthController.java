package org.example.hotforge.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.hotforge.common.exception.ClientException;
import org.example.hotforge.common.result.Result;
import org.example.hotforge.common.result.ResultCode;
import org.example.hotforge.dto.ResetPasswordReqDTO;
import org.example.hotforge.dto.VerificationCodeReqDTO;
import org.example.hotforge.entity.User;
import org.example.hotforge.mapper.UserMapper;
import org.example.hotforge.service.UserService;
import org.example.hotforge.service.VerificationCodeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final VerificationCodeService codes;
    private final UserMapper users;
    private final UserService userService;

    @PostMapping("/verification-codes")
    public Result<String> issue(@Valid @RequestBody VerificationCodeReqDTO request) {
        boolean exists = users.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, request.getPhone())) > 0;
        if ("REGISTER".equals(request.getPurpose()) && exists) {
            throw new ClientException(ResultCode.PHONE_EXISTS);
        }
        if ("RESET_PASSWORD".equals(request.getPurpose()) && !exists) {
            throw new ClientException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return Result.success("模拟验证，不验证真实手机号所有权", codes.issue(request.getPhone(), request.getPurpose()));
    }

    @PostMapping("/password/reset")
    public Result<Void> reset(@Valid @RequestBody ResetPasswordReqDTO request) {
        userService.resetPassword(request);
        return Result.success("密码重置成功", null);
    }
}
