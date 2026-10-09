package org.example.hotforge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.example.hotforge.common.exception.ClientException;
import org.example.hotforge.common.result.ResultCode;
import org.example.hotforge.dto.CourseDraftReqDTO;
import org.example.hotforge.entity.Course;
import org.example.hotforge.entity.User;
import org.example.hotforge.mapper.CourseMapper;
import org.example.hotforge.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseManagementService {
    private final CourseMapper courses;
    private final UserMapper users;

    public Course create(Long userId, CourseDraftReqDTO request) {
        requireTrainer(userId);
        Course course = new Course();
        course.setPublisherId(userId);
        course.setType("NORMAL");
        applyDraft(course, request);
        course.setStatus("DRAFT");
        course.setIsDeleted(0);
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(course.getCreatedAt());
        courses.insert(course);
        return course;
    }

    public List<Course> mine(Long userId) {
        requireTrainer(userId);
        return courses.selectList(new LambdaQueryWrapper<Course>()
                .eq(Course::getPublisherId, userId)
                .eq(Course::getType, "NORMAL")
                .eq(Course::getIsDeleted, 0)
                .orderByDesc(Course::getCreatedAt));
    }

    public Course update(Long userId, Long id, CourseDraftReqDTO request) {
        requireTrainer(userId);
        Course course = ownedCourse(userId, id);
        if (!"DRAFT".equals(course.getStatus()) && !"REJECTED".equals(course.getStatus())) {
            throw new ClientException(ResultCode.BAD_REQUEST, "当前状态不可修改");
        }
        int changed = courses.update(null, new LambdaUpdateWrapper<Course>()
                .eq(Course::getId, id)
                .eq(Course::getPublisherId, userId)
                .eq(Course::getStatus, course.getStatus())
                .eq(Course::getIsDeleted, 0)
                .set(Course::getName, request.getName())
                .set(Course::getDescription, request.getDescription())
                .set(Course::getCoverImage, request.getCoverImage())
                .set(Course::getDuration, request.getDuration())
                .set(Course::getIsVipOnly, request.getIsVipOnly())
                .set(Course::getUpdatedAt, LocalDateTime.now()));
        if (changed != 1) {
            throw new ClientException(ResultCode.BAD_REQUEST, "课程状态已变化");
        }
        return ownedCourse(userId, id);
    }

    public void submit(Long userId, Long id) {
        requireTrainer(userId);
        Course course = ownedCourse(userId, id);
        if (!"DRAFT".equals(course.getStatus()) && !"REJECTED".equals(course.getStatus())) {
            throw new ClientException(ResultCode.BAD_REQUEST, "当前状态不可提交审核");
        }
        int changed = courses.update(null, new LambdaUpdateWrapper<Course>()
                .eq(Course::getId, id)
                .eq(Course::getPublisherId, userId)
                .eq(Course::getStatus, course.getStatus())
                .eq(Course::getIsDeleted, 0)
                .set(Course::getStatus, "PENDING")
                .set(Course::getAuditComment, null)
                .set(Course::getUpdatedAt, LocalDateTime.now()));
        if (changed != 1) {
            throw new ClientException(ResultCode.BAD_REQUEST, "课程状态已变化");
        }
    }

    private Course ownedCourse(Long userId, Long id) {
        Course course = courses.selectOne(new LambdaQueryWrapper<Course>()
                .eq(Course::getId, id)
                .eq(Course::getPublisherId, userId)
                .eq(Course::getType, "NORMAL")
                .eq(Course::getIsDeleted, 0));
        if (course == null) {
            throw new ClientException(ResultCode.NOT_FOUND, "课程不存在");
        }
        return course;
    }

    private void requireTrainer(Long userId) {
        User user = users.selectById(userId);
        if (user == null || !Integer.valueOf(1).equals(user.getStatus())
                || !"APPROVED".equals(user.getTrainerStatus())) {
            throw new ClientException(ResultCode.FORBIDDEN, "仅认证教练可以管理课程");
        }
    }

    private void applyDraft(Course course, CourseDraftReqDTO request) {
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setCoverImage(request.getCoverImage());
        course.setDuration(request.getDuration());
        course.setIsVipOnly(request.getIsVipOnly());
    }
}
