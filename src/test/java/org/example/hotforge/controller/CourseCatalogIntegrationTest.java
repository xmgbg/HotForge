package org.example.hotforge.controller;

import org.example.hotforge.entity.Course;
import org.example.hotforge.mapper.CourseMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(statements = "DELETE FROM `course`", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class CourseCatalogIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private CourseMapper courses;

    @Test
    void guestOnlySeesPublishedCoursesAndNeverReceivesVideoUrl() throws Exception {
        Course published = course("已上架课程", "PUBLISHED", 0);
        course("待审核课程", "PENDING", 0);
        course("已删除课程", "PUBLISHED", 1);

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("已上架课程"))
                .andExpect(jsonPath("$.data[0].videoUrl").doesNotExist());

        mockMvc.perform(get("/api/courses/{id}", published.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("已上架课程"))
                .andExpect(jsonPath("$.data.videoUrl").doesNotExist());
    }

    @Test
    void unpublishedCourseIsNotVisibleToGuest() throws Exception {
        Course draft = course("草稿课程", "DRAFT", 0);
        mockMvc.perform(get("/api/courses/{id}", draft.getId()))
                .andExpect(jsonPath("$.code").value(404));
    }

    private Course course(String name, String status, int deleted) {
        Course course = new Course();
        course.setPublisherId(1L);
        course.setName(name);
        course.setType("NORMAL");
        course.setDuration(20);
        course.setVideoUrl("private/video.mp4");
        course.setIsVipOnly(0);
        course.setStatus(status);
        course.setIsDeleted(deleted);
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());
        courses.insert(course);
        return course;
    }
}
