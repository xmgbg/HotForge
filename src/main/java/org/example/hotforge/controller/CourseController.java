package org.example.hotforge.controller;

import lombok.RequiredArgsConstructor;
import org.example.hotforge.common.result.Result;
import org.example.hotforge.dto.CourseRespDTO;
import org.example.hotforge.service.CourseCatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {
    private final CourseCatalogService service;

    @GetMapping
    public Result<List<CourseRespDTO>> list() {
        return Result.success(service.list());
    }

    @GetMapping("/{id}")
    public Result<CourseRespDTO> get(@PathVariable Long id) {
        return Result.success(service.get(id));
    }
}
