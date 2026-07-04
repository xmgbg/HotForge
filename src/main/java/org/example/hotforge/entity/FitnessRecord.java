package org.example.hotforge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("fitness_record")
public class FitnessRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private BigDecimal weight;

    private BigDecimal bodyFat;

    private BigDecimal chest;

    private BigDecimal waist;

    private BigDecimal hip;

    private BigDecimal arm;

    private BigDecimal thigh;

    private BigDecimal sleepHours;

    private Integer heartRate;

    private Integer steps;

    private LocalDate recordDate;

    private String note;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}