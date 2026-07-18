-- =============================================
-- 热练·即刻健身 (HotForge) 数据库建表脚本
-- MySQL 8.0+
-- 字符集: utf8mb4  引擎: InnoDB
-- =============================================

CREATE DATABASE IF NOT EXISTS hotforge
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE hotforge;

-- =============================================
-- 1. 用户表
-- =============================================
CREATE TABLE `user` (
                        `id`              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '用户ID',
                        `phone`           VARCHAR(20)   NOT NULL                 COMMENT '手机号',
                        `password`        VARCHAR(255)  NOT NULL                 COMMENT '密码(BCrypt加密)',
                        `nickname`        VARCHAR(50)   DEFAULT NULL             COMMENT '昵称',
                        `avatar`          VARCHAR(500)  DEFAULT NULL             COMMENT '头像URL',
                        `role`            VARCHAR(20)   NOT NULL DEFAULT 'USER'  COMMENT '角色: USER-普通用户, VIP-VIP用户, TRAINER-教练, ADMIN-管理员',
                        `vip_expire_time` DATETIME      DEFAULT NULL             COMMENT 'VIP到期时间',
                        `status`          TINYINT       NOT NULL DEFAULT 1       COMMENT '状态: 1=正常, 0=禁用',
                        `created_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
                        `updated_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_phone` (`phone`),
                        KEY `idx_role` (`role`),
                        KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';


-- =============================================
-- 2. 教练认证申请表
-- =============================================
CREATE TABLE `trainer_application` (
                                       `id`                   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '申请ID',
                                       `user_id`              BIGINT       NOT NULL                COMMENT '申请人用户ID',
                                       `real_name`            VARCHAR(50)  NOT NULL                COMMENT '真实姓名',
                                       `id_card`              VARCHAR(18)  NOT NULL                COMMENT '身份证号',
                                       `certification_photos` JSON         DEFAULT NULL            COMMENT '认证材料图片URL列表 ["url1","url2"]',
                                       `bio`                  VARCHAR(500) DEFAULT NULL            COMMENT '个人简介',
                                       `status`               VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '审核状态: PENDING-待审核, APPROVED-已通过, REJECTED-已驳回',
                                       `review_comment`       VARCHAR(500) DEFAULT NULL            COMMENT '审核意见(驳回时填写)',
                                       `reviewed_by`          BIGINT       DEFAULT NULL            COMMENT '审核人(管理员ID)',
                                       `reviewed_at`          DATETIME     DEFAULT NULL            COMMENT '审核时间',
                                       `created_at`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
                                       `updated_at`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       PRIMARY KEY (`id`),
                                       KEY `idx_user_id` (`user_id`),
                                       KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教练认证申请表';


-- =============================================
-- 3. 课程表
-- 三种课程类型（每种都可设为 VIP 专享）：
--   OFFICIAL - 官方网课（录播，管理员发布，随时可练，免审核）
--   NORMAL   - 教练网课（录播，教练发布，随时可练，需审核）
--   LIVE     - 直播抢课（教练发布，需排期+抢名额，需审核）
-- =============================================
CREATE TABLE `course` (
                          `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '课程ID',
                          `publisher_id`  BIGINT        NOT NULL                COMMENT '发布者ID(OFFICIAL=管理员, NORMAL/LIVE=教练)',
                          `name`          VARCHAR(100)  NOT NULL                COMMENT '课程名称',
                          `type`          VARCHAR(20)   NOT NULL                COMMENT '课程类型: OFFICIAL-官方网课, NORMAL-教练网课, LIVE-直播抢课',
                          `description`   TEXT          DEFAULT NULL            COMMENT '课程简介',
                          `cover_image`   VARCHAR(500)  DEFAULT NULL            COMMENT '封面图URL',
                          `video_url`     VARCHAR(500)  DEFAULT NULL            COMMENT '录播视频URL(OFFICIAL/NORMAL 必填, LIVE 为空)',
                          `duration`      INT           NOT NULL                COMMENT '课程时长(分钟)',
                          `max_capacity`  INT           DEFAULT NULL            COMMENT '每节最大名额(仅 LIVE 有效, 网课不限)',
                          `is_vip_only`   TINYINT(1)    NOT NULL DEFAULT 0      COMMENT '是否VIP专享: 0=否, 1=是',
                          `status`        VARCHAR(20)   NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT-草稿, PENDING-待审核, PUBLISHED-已上架, REJECTED-已驳回, OFFLINE-已下架(OFFICIAL 可直接 PUBLISHED)',
                          `audit_comment` VARCHAR(500)  DEFAULT NULL            COMMENT '审核意见',
                          `is_deleted`    TINYINT(1)    NOT NULL DEFAULT 0      COMMENT '逻辑删除: 0=正常, 1=已删除',
                          `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          PRIMARY KEY (`id`),
                          KEY `idx_publisher_id` (`publisher_id`),
                          KEY `idx_type` (`type`),
                          KEY `idx_status` (`status`),
                          KEY `idx_is_vip_only` (`is_vip_only`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';


-- =============================================
-- 4. 课程时段表（仅服务 LIVE 直播抢课的排期，网课无排期）
-- =============================================
CREATE TABLE `course_session` (
                                  `id`             BIGINT      NOT NULL AUTO_INCREMENT COMMENT '时段ID',
                                  `course_id`      BIGINT      NOT NULL               COMMENT '课程ID(必须是 LIVE 类型)',
                                  `publisher_id`   BIGINT      NOT NULL               COMMENT '发布者ID(冗余自 course, 方便查询)',
                                  `start_time`     DATETIME    NOT NULL               COMMENT '开始时间',
                                  `end_time`       DATETIME    NOT NULL               COMMENT '结束时间',
                                  `capacity`       INT         NOT NULL               COMMENT '剩余名额(Redis为准，MySQL异步同步)',
                                  `total_capacity` INT         NOT NULL               COMMENT '总名额',
                                  `live_url`       VARCHAR(500) DEFAULT NULL          COMMENT '直播间地址(开播前填写)',
                                  `status`         VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING-待审核, AVAILABLE-可抢, FULL-已满, CANCELLED-已取消, COMPLETED-已结束',
                                  `audit_comment`  VARCHAR(500) DEFAULT NULL          COMMENT '审核意见',
                                  `is_deleted`     TINYINT(1)  NOT NULL DEFAULT 0     COMMENT '逻辑删除: 0=正常, 1=已删除',
                                  `created_at`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `updated_at`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  PRIMARY KEY (`id`),
                                  KEY `idx_course_id` (`course_id`),
                                  KEY `idx_publisher_id` (`publisher_id`),
                                  KEY `idx_start_time` (`start_time`),
                                  KEY `idx_status_start` (`status`, `start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程时段表(仅LIVE直播抢课)';


-- =============================================
-- 5. 预约记录表（仅记录 LIVE 直播抢课的抢课结果，网课无预约）
-- =============================================
CREATE TABLE `booking` (
                           `id`           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '预约ID',
                           `user_id`      BIGINT      NOT NULL               COMMENT '用户ID',
                           `session_id`   BIGINT      NOT NULL               COMMENT '时段ID',
                           `course_id`    BIGINT      NOT NULL               COMMENT '课程ID(冗余，方便查询)',
                           `status`       VARCHAR(30) NOT NULL DEFAULT 'BOOKED' COMMENT '状态: BOOKED-已预约, COMPLETED-已完成, CANCELLED-用户取消, CANCELLED_BY_TRAINER-教练取消',
                           `booked_at`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '预约时间',
                           `cancelled_at` DATETIME    DEFAULT NULL           COMMENT '取消时间',
                           `created_at`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `updated_at`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `uk_user_session` (`user_id`, `session_id`),
                           KEY `idx_user_id` (`user_id`),
                           KEY `idx_session_id` (`session_id`),
                           KEY `idx_course_id` (`course_id`),
                           KEY `idx_status` (`status`),
                           KEY `idx_booked_at` (`booked_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约记录表';


-- =============================================
-- 6. 体测记录表
-- =============================================
CREATE TABLE `fitness_record` (
                                  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
                                  `user_id`      BIGINT       NOT NULL               COMMENT '用户ID',
                                  `weight`       DECIMAL(5,1) DEFAULT NULL            COMMENT '体重(kg)',
                                  `body_fat`     DECIMAL(4,1) DEFAULT NULL            COMMENT '体脂率(%)',
                                  `chest`        DECIMAL(5,1) DEFAULT NULL            COMMENT '胸围(cm)',
                                  `waist`        DECIMAL(5,1) DEFAULT NULL            COMMENT '腰围(cm)',
                                  `hip`          DECIMAL(5,1) DEFAULT NULL            COMMENT '臀围(cm)',
                                  `arm`          DECIMAL(5,1) DEFAULT NULL            COMMENT '臂围(cm)',
                                  `thigh`        DECIMAL(5,1) DEFAULT NULL            COMMENT '腿围(cm)',
                                  `sleep_hours`  DECIMAL(3,1) DEFAULT NULL            COMMENT '睡眠时长(h, VIP专属)',
                                  `heart_rate`   INT          DEFAULT NULL            COMMENT '静息心率(bpm, VIP专属)',
                                  `steps`        INT          DEFAULT NULL            COMMENT '日步数(步, VIP专属)',
                                  `record_date`  DATE         NOT NULL                COMMENT '记录日期',
                                  `note`         VARCHAR(500) DEFAULT NULL            COMMENT '备注',
                                  `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `uk_user_date` (`user_id`, `record_date`),
                                  KEY `idx_user_id` (`user_id`),
                                  KEY `idx_record_date` (`record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='体测记录表';


-- =============================================
-- 7. VIP 购买记录表
-- =============================================
CREATE TABLE `vip_order` (
                             `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '订单ID',
                             `user_id`         BIGINT       NOT NULL               COMMENT '用户ID',
                             `duration_months` INT          NOT NULL               COMMENT '购买月数',
                             `amount`          DECIMAL(10,2) DEFAULT NULL          COMMENT '实付金额(元)',
                             `start_time`      DATETIME     NOT NULL               COMMENT 'VIP生效时间',
                             `end_time`        DATETIME     NOT NULL               COMMENT 'VIP到期时间',
                             `status`          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE-有效, EXPIRED-已过期, REFUNDED-已退款',
                             `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '购买时间',
                             PRIMARY KEY (`id`),
                             KEY `idx_user_id` (`user_id`),
                             KEY `idx_end_time` (`end_time`),
                             KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='VIP购买记录表';


-- =============================================
-- 8. 系统操作日志表
-- =============================================
CREATE TABLE `operation_log` (
                                 `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '日志ID',
                                 `user_id`       BIGINT        DEFAULT NULL            COMMENT '操作用户ID',
                                 `action`        VARCHAR(50)   NOT NULL                COMMENT '操作类型: LOGIN/BOOK/CANCEL/CREATE_COURSE/AUDIT...',
                                 `target_type`   VARCHAR(50)   DEFAULT NULL            COMMENT '操作对象类型: USER/COURSE/SESSION/BOOKING...',
                                 `target_id`     BIGINT        DEFAULT NULL            COMMENT '操作对象ID',
                                 `detail`        VARCHAR(1000) DEFAULT NULL            COMMENT '操作详情(JSON)',
                                 `ip`            VARCHAR(50)   DEFAULT NULL            COMMENT '操作IP',
                                 `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
                                 PRIMARY KEY (`id`),
                                 KEY `idx_user_id` (`user_id`),
                                 KEY `idx_action` (`action`),
                                 KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统操作日志表';


-- 管理员账号不在仓库中预置，请在部署时通过安全流程创建。
