package org.example.hotforge.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.hotforge.common.result.Result;
import org.example.hotforge.dto.CourseDraftReqDTO;
import org.example.hotforge.entity.Course;
import org.example.hotforge.service.CourseManagementService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/me/courses")
@RequiredArgsConstructor
public class TrainerCourseController {
    private final CourseManagementService service;

    @PostMapping
    public Result<Course> create(@Valid @RequestBody CourseDraftReqDTO request) {
        return Result.success(service.create(currentUserId(), request));
    }

    @GetMapping
    public Result<List<Course>> mine() {
        return Result.success(service.mine(currentUserId()));
    }

    @PutMapping("/{id}")
    public Result<Course> update(@PathVariable Long id, @Valid @RequestBody CourseDraftReqDTO request) {
        return Result.success(service.update(currentUserId(), id, request));
    }

    @PostMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Long id) {
        service.submit(currentUserId(), id);
        return Result.success("提交审核成功", null);
    }

    private Long currentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
