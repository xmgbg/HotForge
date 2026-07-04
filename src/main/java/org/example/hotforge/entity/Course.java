package org.example.hotforge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("course")
public class Course {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long trainerId;

    private String name;

    private String type;

    private String description;

    private String coverImage;

    private Integer duration;

    private Integer maxCapacity;

    private Integer isVipOnly;

    private String status;

    private String auditComment;

    private Integer isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
