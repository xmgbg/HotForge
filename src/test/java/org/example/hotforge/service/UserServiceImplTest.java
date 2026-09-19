package org.example.hotforge.service;

import org.example.hotforge.common.exception.ClientException;
import org.example.hotforge.common.result.ResultCode;
import org.example.hotforge.dto.LoginReqDTO;
import org.example.hotforge.dto.LoginRespDTO;
import org.example.hotforge.dto.RegisterReqDTO;
import org.example.hotforge.dto.UpdateUserReqDTO;
import org.example.hotforge.entity.User;
import org.example.hotforge.mapper.UserMapper;
import org.example.hotforge.service.impl.UserServiceImpl;
import org.example.hotforge.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userMapper, passwordEncoder, jwtUtil);
    }

    @Test
    void registerShouldEncryptPasswordAndReturnToken() {
        RegisterReqDTO request = new RegisterReqDTO();
        request.setPhone("13800138000");
        request.setPassword("secret12");

        when(userMapper.selectOne(any())).thenReturn(null);
        when(passwordEncoder.encode("secret12")).thenReturn("encoded-password");
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return 1;
        });
        when(jwtUtil.generateToken(1L, "13800138000", "USER")).thenReturn("jwt-token");

        LoginRespDTO response = userService.register(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUser().getNickname()).isEqualTo("138****8000");
        assertThat(response.getUser().getRole()).isEqualTo("USER");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("encoded-password");
    }

    @Test
    void registerShouldRejectExistingPhone() {
        RegisterReqDTO request = new RegisterReqDTO();
        request.setPhone("13800138000");
        request.setPassword("secret12");
        when(userMapper.selectOne(any())).thenReturn(new User());

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(ClientException.class)
                .extracting("resultCode")
                .isEqualTo(ResultCode.PHONE_EXISTS);
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void loginShouldReturnTokenForValidCredentials() {
        User user = activeUser();
        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("secret12", "encoded-password")).thenReturn(true);
        when(jwtUtil.generateToken(1L, "13800138000", "USER")).thenReturn("jwt-token");

        LoginRespDTO response = userService.login(loginRequest());

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUser().getId()).isEqualTo(1L);
    }

    @Test
    void loginShouldRejectWrongPassword() {
        when(userMapper.selectOne(any())).thenReturn(activeUser());
        when(passwordEncoder.matches("secret12", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> userService.login(loginRequest()))
                .isInstanceOf(ClientException.class)
                .extracting("resultCode")
                .isEqualTo(ResultCode.LOGIN_FAILED);
    }

    @Test
    void loginShouldRejectDisabledAccount() {
        User user = activeUser();
        user.setStatus(0);
        when(userMapper.selectOne(any())).thenReturn(user);

        assertThatThrownBy(() -> userService.login(loginRequest()))
                .isInstanceOf(ClientException.class)
                .extracting("resultCode")
                .isEqualTo(ResultCode.ACCOUNT_DISABLED);
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void getCurrentUserShouldReturnSafeProfile() {
        when(userMapper.selectById(1L)).thenReturn(activeUser());

        assertThat(userService.getCurrentUser(1L))
                .extracting("id", "phone", "nickname")
                .containsExactly(1L, "13800138000", "测试用户");
    }

    @Test
    void updateCurrentUserShouldPersistAllowedFields() {
        User user = activeUser();
        UpdateUserReqDTO request = new UpdateUserReqDTO();
        request.setNickname("新昵称");
        request.setAvatar("https://example.com/avatar.png");
        when(userMapper.selectById(1L)).thenReturn(user);

        userService.updateCurrentUser(1L, request);

        assertThat(user.getNickname()).isEqualTo("新昵称");
        assertThat(user.getAvatar()).isEqualTo("https://example.com/avatar.png");
        verify(userMapper).updateById(user);
    }

    private LoginReqDTO loginRequest() {
        LoginReqDTO request = new LoginReqDTO();
        request.setPhone("13800138000");
        request.setPassword("secret12");
        return request;
    }

    private User activeUser() {
        User user = new User();
        user.setId(1L);
        user.setPhone("13800138000");
        user.setPassword("encoded-password");
        user.setNickname("测试用户");
        user.setRole("USER");
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.of(2026, 9, 19, 12, 0));
        return user;
    }
}
