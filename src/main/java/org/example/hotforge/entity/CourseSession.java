package org.example.hotforge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("course_session")
public class CourseSession {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long courseId;

    private Long publisherId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer capacity;

    private Integer totalCapacity;

    private String liveUrl;

    private String status;

    private String auditComment;

    private Integer isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}