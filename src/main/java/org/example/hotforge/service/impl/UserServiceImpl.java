package org.example.hotforge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hotforge.common.exception.ClientException;
import org.example.hotforge.common.result.ResultCode;
import org.example.hotforge.dto.LoginReqDTO;
import org.example.hotforge.dto.LoginRespDTO;
import org.example.hotforge.dto.RegisterReqDTO;
import org.example.hotforge.dto.ResetPasswordReqDTO;
import org.example.hotforge.dto.UpdateUserReqDTO;
import org.example.hotforge.dto.UserRespDTO;
import org.example.hotforge.entity.User;
import org.example.hotforge.mapper.UserMapper;
import org.example.hotforge.service.UserService;
import org.example.hotforge.service.VerificationCodeService;
import org.example.hotforge.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * ================================
 * 用户业务实现
 * ================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final VerificationCodeService verificationCodeService;

    // ==========================================
    // 注册
    // ==========================================
    @Override
    public LoginRespDTO register(RegisterReqDTO dto) {
        // ① 手机号唯一性校验
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, dto.getPhone());
        if (userMapper.selectOne(wrapper) != null) {
            throw new ClientException(ResultCode.PHONE_EXISTS);
        }
        verificationCodeService.consume(dto.getPhone(), "REGISTER", dto.getCode());

        // ② 构造 User 实体
        User user = new User();
        user.setPhone(dto.getPhone());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null && !dto.getNickname().isBlank()
                ? dto.getNickname()
                : maskPhone(dto.getPhone()));
        user.setRole("USER");
        user.setTrainerStatus("NONE");
        user.setTokenVersion(0);
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // ③ 入库
        userMapper.insert(user);
        log.info("新用户注册成功, userId={}, phone={}", user.getId(), dto.getPhone());

        // ④ 签发 Token + 构造响应
        String token = jwtUtil.generateToken(user.getId(), user.getPhone(), user.getRole(), user.getTokenVersion());
        return LoginRespDTO.builder()
                .token(token)
                .user(buildUserResp(user))
                .build();
    }

    // ==========================================
    // 登录
    // ==========================================
    @Override
    public LoginRespDTO login(LoginReqDTO dto) {
        // ① 按手机号查用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, dto.getPhone());
        User user = userMapper.selectOne(wrapper);

        // ② 用户不存在
        if (user == null) {
            throw new ClientException(ResultCode.LOGIN_FAILED);
        }

        // ③ 检查账号是否被禁用
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new ClientException(ResultCode.ACCOUNT_DISABLED);
        }

        // ④ 比对密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new ClientException(ResultCode.LOGIN_FAILED);
        }

        // ⑤ 签发 Token
        String token = jwtUtil.generateToken(user.getId(), user.getPhone(), user.getRole(), user.getTokenVersion());
        log.info("用户登录成功, userId={}, role={}", user.getId(), user.getRole());

        return LoginRespDTO.builder()
                .token(token)
                .user(buildUserResp(user))
                .build();
    }

    // ==========================================
    // 获取当前用户信息
    // ==========================================
    @Override
    public UserRespDTO getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ClientException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return buildUserResp(user);
    }

    @Override
    public void resetPassword(ResetPasswordReqDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, dto.getPhone());
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new ClientException(ResultCode.NOT_FOUND, "用户不存在");
        }
        verificationCodeService.consume(dto.getPhone(), "RESET_PASSWORD", dto.getCode());
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setTokenVersion(user.getTokenVersion() + 1);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    // ==========================================
    // 修改个人信息
    // ==========================================
    @Override
    public void updateCurrentUser(Long userId, UpdateUserReqDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ClientException(ResultCode.NOT_FOUND, "用户不存在");
        }

        boolean updated = false;

        if (dto.getNickname() != null && !dto.getNickname().isBlank()) {
            user.setNickname(dto.getNickname());
            updated = true;
        }
        if (dto.getAvatar() != null && !dto.getAvatar().isBlank()) {
            user.setAvatar(dto.getAvatar());
            updated = true;
        }

        if (!updated) {
            throw new ClientException(ResultCode.BAD_REQUEST, "至少需要修改一个字段");
        }

        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("用户信息修改成功, userId={}", userId);
    }

    // ==========================================
    // 内部工具方法
    // ==========================================

    /** 手机号脱敏：13812345678 → 138****5678 */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /** User 实体 → UserRespDTO（不含密码） */
    private UserRespDTO buildUserResp(User user) {
        return UserRespDTO.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .trainerStatus(user.getTrainerStatus())
                .vipExpireTime(user.getVipExpireTime())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
