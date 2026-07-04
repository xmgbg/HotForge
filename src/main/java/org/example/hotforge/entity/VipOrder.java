package org.example.hotforge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("vip_order")
public class VipOrder {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer durationMonths;

    private BigDecimal amount;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String status;

    private LocalDateTime createdAt;
}