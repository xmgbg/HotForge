package org.example.hotforge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.hotforge.common.exception.ClientException;
import org.example.hotforge.common.result.ResultCode;
import org.example.hotforge.dto.CourseRespDTO;
import org.example.hotforge.entity.Course;
import org.example.hotforge.mapper.CourseMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseCatalogService {
    private final CourseMapper courseMapper;

    public List<CourseRespDTO> list() {
        return courseMapper.selectList(new LambdaQueryWrapper<Course>()
                        .eq(Course::getStatus, "PUBLISHED")
                        .eq(Course::getIsDeleted, 0)
                        .orderByDesc(Course::getCreatedAt)
                        .last("LIMIT 20"))
                .stream().map(this::toResponse).toList();
    }

    public CourseRespDTO get(Long id) {
        Course course = courseMapper.selectOne(new LambdaQueryWrapper<Course>()
                .eq(Course::getId, id)
                .eq(Course::getStatus, "PUBLISHED")
                .eq(Course::getIsDeleted, 0));
        if (course == null) {
            throw new ClientException(ResultCode.NOT_FOUND, "课程不存在或未上架");
        }
        return toResponse(course);
    }

    private CourseRespDTO toResponse(Course course) {
        return CourseRespDTO.builder()
                .id(course.getId())
                .publisherId(course.getPublisherId())
                .name(course.getName())
                .type(course.getType())
                .description(course.getDescription())
                .coverImage(course.getCoverImage())
                .duration(course.getDuration())
                .isVipOnly(course.getIsVipOnly())
                .build();
    }
}
