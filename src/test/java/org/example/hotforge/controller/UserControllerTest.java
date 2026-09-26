package org.example.hotforge.controller;

import org.example.hotforge.config.JwtAuthenticationFilter;
import org.example.hotforge.config.JwtProperties;
import org.example.hotforge.config.SecurityConfig;
import org.example.hotforge.dto.LoginRespDTO;
import org.example.hotforge.dto.UserRespDTO;
import org.example.hotforge.service.UserService;
import org.example.hotforge.mapper.UserMapper;
import org.example.hotforge.entity.User;
import org.example.hotforge.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtProperties.class, JwtUtil.class})
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserMapper userMapper;

    @org.junit.jupiter.api.BeforeEach
    void authenticatedUser() {
        User user = new User();
        user.setId(1L);
        user.setRole("USER");
        user.setStatus(1);
        user.setTokenVersion(0);
        org.mockito.Mockito.lenient().when(userMapper.selectById(1L)).thenReturn(user);
    }

    @Test
    void registerShouldBePublic() throws Exception {
        when(userService.register(any())).thenReturn(loginResponse());

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"phone":"13800138000","password":"secret12","code":"123456","nickname":"测试用户"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("jwt-token"));
    }

    @Test
    void loginShouldBePublic() throws Exception {
        when(userService.login(any())).thenReturn(loginResponse());

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"phone":"13800138000","password":"secret12"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("登录成功"));
    }

    @Test
    void profileShouldRejectAnonymousRequest() throws Exception {
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void profileShouldUseUserIdFromJwt() throws Exception {
        when(userService.getCurrentUser(1L)).thenReturn(userResponse());

        mockMvc.perform(get("/api/user/me")
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.phone").value("13800138000"));
    }

    @Test
    void updateProfileShouldAcceptAuthenticatedRequest() throws Exception {
        doNothing().when(userService).updateCurrentUser(eq(1L), any());

        mockMvc.perform(put("/api/user/me")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nickname":"新昵称","avatar":"https://example.com/avatar.png"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("修改成功"));
    }

    private String token() {
        return jwtUtil.generateToken(1L, "13800138000", "USER", 0);
    }

    private LoginRespDTO loginResponse() {
        return LoginRespDTO.builder()
                .token("jwt-token")
                .user(userResponse())
                .build();
    }

    private UserRespDTO userResponse() {
        return UserRespDTO.builder()
                .id(1L)
                .phone("13800138000")
                .nickname("测试用户")
                .role("USER")
                .status(1)
                .build();
    }
}
