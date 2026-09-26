package org.example.hotforge.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.hotforge.common.result.Result;
import org.example.hotforge.dto.TrainerApplicationReqDTO;
import org.example.hotforge.dto.TrainerReviewReqDTO;
import org.example.hotforge.entity.TrainerApplication;
import org.example.hotforge.service.TrainerApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TrainerApplicationController {
    private final TrainerApplicationService service;

    @PostMapping("/api/trainer-applications")
    public Result<TrainerApplication> apply(@Valid @RequestBody TrainerApplicationReqDTO request) {
        return Result.success(service.apply(currentUserId(), request));
    }

    @GetMapping("/api/me/trainer-applications")
    public Result<List<TrainerApplication>> mine() {
        return Result.success(service.mine(currentUserId()));
    }

    @GetMapping("/api/admin/trainer-applications")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<TrainerApplication>> pending() {
        return Result.success(service.pending());
    }

    @PostMapping("/api/admin/trainer-applications/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> review(@PathVariable Long id, @Valid @RequestBody TrainerReviewReqDTO request) {
        service.review(currentUserId(), id, request);
        return Result.success("审核完成", null);
    }

    private Long currentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
