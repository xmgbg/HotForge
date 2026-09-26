package org.example.hotforge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.example.hotforge.common.exception.ClientException;
import org.example.hotforge.common.result.ResultCode;
import org.example.hotforge.dto.TrainerApplicationReqDTO;
import org.example.hotforge.dto.TrainerReviewReqDTO;
import org.example.hotforge.entity.TrainerApplication;
import org.example.hotforge.entity.User;
import org.example.hotforge.mapper.TrainerApplicationMapper;
import org.example.hotforge.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainerApplicationService {
    private final UserMapper users;
    private final TrainerApplicationMapper applications;
    private final ObjectMapper objectMapper;

    @Transactional
    public TrainerApplication apply(Long userId, TrainerApplicationReqDTO request) {
        User user = users.selectById(userId);
        if (user == null || !Integer.valueOf(1).equals(user.getStatus())) {
            throw new ClientException(ResultCode.FORBIDDEN, "账号不可申请教练认证");
        }
        LocalDateTime now = LocalDateTime.now();
        int changed = users.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .in(User::getTrainerStatus, "NONE", "REJECTED")
                .set(User::getTrainerStatus, "PENDING")
                .set(User::getUpdatedAt, now));
        if (changed != 1) {
            throw new ClientException(ResultCode.BAD_REQUEST, "当前资格状态不可重复申请");
        }
        TrainerApplication application = new TrainerApplication();
        application.setUserId(userId);
        application.setRealName(request.getRealName());
        application.setCertificationPhotos(objectMapper.writeValueAsString(request.getCertificationPhotos()));
        application.setBio(request.getBio());
        application.setStatus("PENDING");
        application.setCreatedAt(now);
        application.setUpdatedAt(now);
        applications.insert(application);
        return application;
    }

    public List<TrainerApplication> mine(Long userId) {
        return applications.selectList(new LambdaQueryWrapper<TrainerApplication>()
                .eq(TrainerApplication::getUserId, userId)
                .orderByDesc(TrainerApplication::getCreatedAt));
    }

    public List<TrainerApplication> pending() {
        return applications.selectList(new LambdaQueryWrapper<TrainerApplication>()
                .eq(TrainerApplication::getStatus, "PENDING")
                .orderByAsc(TrainerApplication::getCreatedAt));
    }

    @Transactional
    public void review(Long adminId, Long applicationId, TrainerReviewReqDTO request) {
        if ("REJECTED".equals(request.getDecision())
                && (request.getComment() == null || request.getComment().isBlank())) {
            throw new ClientException(ResultCode.BAD_REQUEST, "驳回时需要填写审核意见");
        }
        TrainerApplication application = applications.selectById(applicationId);
        if (application == null) {
            throw new ClientException(ResultCode.NOT_FOUND, "认证申请不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        int changed = applications.update(null, new LambdaUpdateWrapper<TrainerApplication>()
                .eq(TrainerApplication::getId, applicationId)
                .eq(TrainerApplication::getStatus, "PENDING")
                .set(TrainerApplication::getStatus, request.getDecision())
                .set(TrainerApplication::getReviewComment, request.getComment())
                .set(TrainerApplication::getReviewedBy, adminId)
                .set(TrainerApplication::getReviewedAt, now)
                .set(TrainerApplication::getUpdatedAt, now));
        if (changed != 1) {
            throw new ClientException(ResultCode.BAD_REQUEST, "申请已审核");
        }
        int userChanged = users.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getId, application.getUserId())
                .eq(User::getTrainerStatus, "PENDING")
                .set(User::getTrainerStatus, request.getDecision())
                .set(User::getUpdatedAt, now));
        if (userChanged != 1) {
            throw new ClientException(ResultCode.BAD_REQUEST, "用户资格状态已变化");
        }
    }
}
