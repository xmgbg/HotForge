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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(statements = {"DELETE FROM `course`", "DELETE FROM `user`"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class TrainerCourseIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private UserMapper users;
    @Autowired private JwtUtil jwtUtil;
    @MockitoBean private VerificationCodeService codes;

    private static final String DRAFT = """
            {"name":"力量入门","description":"全身训练","duration":30,"isVipOnly":1}
            """;

    @Test
    void onlyApprovedTrainerCanCreateDraftAndSubmitOwnCourse() throws Exception {
        String trainer = register("13800138101");
        String other = register("13800138102");
        mockMvc.perform(post("/api/me/courses").header("Authorization", bearer(trainer))
                        .contentType(MediaType.APPLICATION_JSON).content(DRAFT))
                .andExpect(jsonPath("$.code").value(403));

        approveTrainer(trainer);
        String response = mockMvc.perform(post("/api/me/courses")
                        .header("Authorization", bearer(trainer))
                        .contentType(MediaType.APPLICATION_JSON).content(DRAFT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.type").value("NORMAL"))
                .andExpect(jsonPath("$.data.isVipOnly").value(1))
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(response, "$.data.id");

        mockMvc.perform(get("/api/courses"))
                .andExpect(jsonPath("$.data.length()").value(0));
        mockMvc.perform(post("/api/me/courses/{id}/submit", id)
                        .header("Authorization", bearer(other)))
                .andExpect(jsonPath("$.code").value(403));

        approveTrainer(other);
        mockMvc.perform(put("/api/me/courses/{id}", id)
                        .header("Authorization", bearer(other))
                        .contentType(MediaType.APPLICATION_JSON).content(DRAFT))
                .andExpect(jsonPath("$.code").value(404));

        mockMvc.perform(put("/api/me/courses/{id}", id)
                        .header("Authorization", bearer(trainer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"力量进阶\",\"duration\":35,\"isVipOnly\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("力量进阶"))
                .andExpect(jsonPath("$.data.description").isEmpty());

        mockMvc.perform(post("/api/me/courses/{id}/submit", id)
                        .header("Authorization", bearer(trainer)))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/me/courses/{id}", id)
                        .header("Authorization", bearer(trainer))
                        .contentType(MediaType.APPLICATION_JSON).content(DRAFT))
                .andExpect(jsonPath("$.code").value(400));
        mockMvc.perform(post("/api/me/courses/{id}/submit", id)
                        .header("Authorization", bearer(trainer)))
                .andExpect(jsonPath("$.code").value(400));
        mockMvc.perform(get("/api/courses"))
                .andExpect(jsonPath("$.data.length()").value(0));
        mockMvc.perform(get("/api/me/courses").header("Authorization", bearer(trainer)))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }

    @Test
    void adminCanApproveOrRejectPendingCourseAndRejectedCourseCanBeResubmitted() throws Exception {
        String trainer = register("13800138103");
        String admin = register("13800138104");
        approveTrainer(trainer);
        User adminUser = users.selectById(jwtUtil.getUserId(admin));
        adminUser.setRole("ADMIN");
        users.updateById(adminUser);

        String response = mockMvc.perform(post("/api/me/courses")
                        .header("Authorization", bearer(trainer))
                        .contentType(MediaType.APPLICATION_JSON).content(DRAFT))
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(response, "$.data.id");
        mockMvc.perform(post("/api/me/courses/{id}/submit", id)
                        .header("Authorization", bearer(trainer)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/courses/pending")
                        .header("Authorization", bearer(trainer)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/courses/pending")
                        .header("Authorization", bearer(admin)))
                .andExpect(jsonPath("$.data[0].id").value(id.intValue()));
        mockMvc.perform(post("/api/admin/courses/{id}/review", id)
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"REJECTED\"}"))
                .andExpect(jsonPath("$.code").value(400));
        mockMvc.perform(post("/api/admin/courses/{id}/review", id)
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"REJECTED\",\"comment\":\"介绍不清晰\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/courses"))
                .andExpect(jsonPath("$.data.length()").value(0));
        mockMvc.perform(get("/api/me/courses").header("Authorization", bearer(trainer)))
                .andExpect(jsonPath("$.data[0].auditComment").value("介绍不清晰"));

        mockMvc.perform(put("/api/me/courses/{id}", id)
                        .header("Authorization", bearer(trainer))
                        .contentType(MediaType.APPLICATION_JSON).content(DRAFT))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/me/courses/{id}/submit", id)
                        .header("Authorization", bearer(trainer)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/me/courses").header("Authorization", bearer(trainer)))
                .andExpect(jsonPath("$.data[0].auditComment").isEmpty());
        mockMvc.perform(post("/api/admin/courses/{id}/review", id)
                        .header("Authorization", bearer(trainer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"APPROVED\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/admin/courses/{id}/review", id)
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"APPROVED\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/courses"))
                .andExpect(jsonPath("$.data[0].id").value(id.intValue()))
                .andExpect(jsonPath("$.data[0].isVipOnly").value(1));
        mockMvc.perform(get("/api/admin/courses/pending")
                        .header("Authorization", bearer(admin)))
                .andExpect(jsonPath("$.data.length()").value(0));
        mockMvc.perform(post("/api/admin/courses/{id}/review", id)
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"REJECTED\",\"comment\":\"重复审核\"}"))
                .andExpect(jsonPath("$.code").value(400));
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

    private void approveTrainer(String token) {
        User user = users.selectById(jwtUtil.getUserId(token));
        user.setTrainerStatus("APPROVED");
        users.updateById(user);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
