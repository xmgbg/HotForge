package org.example.hotforge.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(statements = "DELETE FROM `user`", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCompleteRegisterLoginAndProfileFlow() throws Exception {
        String registerJson = mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"phone":"13800138000","password":"secret12","nickname":"测试用户"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.user.nickname").value("测试用户"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String registerToken = JsonPath.read(registerJson, "$.data.token");

        String loginJson = mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"phone":"13800138000","password":"secret12"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String loginToken = JsonPath.read(loginJson, "$.data.token");

        mockMvc.perform(get("/api/user/me")
                        .header("Authorization", "Bearer " + registerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.phone").value("13800138000"));

        mockMvc.perform(put("/api/user/me")
                        .header("Authorization", "Bearer " + loginToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nickname":"训练达人","avatar":"https://example.com/avatar.png"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("修改成功"));

        mockMvc.perform(get("/api/user/me")
                        .header("Authorization", "Bearer " + loginToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("训练达人"))
                .andExpect(jsonPath("$.data.avatar").value("https://example.com/avatar.png"));
    }
}
