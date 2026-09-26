package org.example.hotforge.controller;

import com.jayway.jsonpath.JsonPath;
import org.example.hotforge.entity.User;
import org.example.hotforge.mapper.UserMapper;
import org.example.hotforge.service.VerificationCodeService;
import org.example.hotforge.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(statements = {"DELETE FROM `trainer_application`", "DELETE FROM `user`"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class TrainerApplicationIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private UserMapper users;
    @Autowired private JwtUtil jwtUtil;
    @MockitoBean private VerificationCodeService codes;

    @Test
    void applicationRequiresAdminReviewAndCannotBeReviewedTwice() throws Exception {
        String userToken = register("13800138001");
        String adminToken = register("13800138002");
        User admin = users.selectById(jwtUtil.getUserId(adminToken));
        admin.setRole("ADMIN");
        users.updateById(admin);

        String applicationJson = mockMvc.perform(post("/api/trainer-applications")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"realName":"测试教练","certificationPhotos":["private/demo.jpg"],"bio":"团课教学"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();
        Number applicationId = JsonPath.read(applicationJson, "$.data.id");

        mockMvc.perform(post("/api/admin/trainer-applications/{id}/review", applicationId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"APPROVED\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/admin/trainer-applications/{id}/review", applicationId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"APPROVED\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/user/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.trainerStatus").value("APPROVED"));

        mockMvc.perform(post("/api/admin/trainer-applications/{id}/review", applicationId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"REJECTED\",\"comment\":\"重复审核\"}"))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void rejectedApplicantCanSubmitAgainButPendingApplicantCannot() throws Exception {
        String userToken = register("13800138003");
        String adminToken = register("13800138004");
        User admin = users.selectById(jwtUtil.getUserId(adminToken));
        admin.setRole("ADMIN");
        users.updateById(admin);

        String body = "{\"realName\":\"测试教练\",\"certificationPhotos\":[\"private/demo.jpg\"]}";
        String response = mockMvc.perform(post("/api/trainer-applications")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(response, "$.data.id");

        mockMvc.perform(post("/api/trainer-applications")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(jsonPath("$.code").value(400));

        mockMvc.perform(post("/api/admin/trainer-applications/{id}/review", id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"REJECTED\",\"comment\":\"材料不清晰\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/trainer-applications")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    private String register(String phone) throws Exception {
        String response = mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"" + phone
                                + "\",\"password\":\"secret12\",\"code\":\"123456\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.data.token");
    }
}
