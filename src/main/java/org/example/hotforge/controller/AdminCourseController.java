package org.example.hotforge.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.hotforge.common.result.Result;
import org.example.hotforge.dto.CourseReviewReqDTO;
import org.example.hotforge.entity.Course;
import org.example.hotforge.service.CourseManagementService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/courses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCourseController {
    private final CourseManagementService service;

    @GetMapping("/pending")
    public Result<List<Course>> pending() {
        return Result.success(service.pending());
    }

    @PostMapping("/{id}/review")
    public Result<Void> review(@PathVariable Long id, @Valid @RequestBody CourseReviewReqDTO request) {
        service.review(id, request);
        return Result.success("审核完成", null);
    }
}
