DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `trainer_application`;
DROP TABLE IF EXISTS `course`;

CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `phone` VARCHAR(20) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `nickname` VARCHAR(50),
    `avatar` VARCHAR(500),
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER',
    `trainer_status` VARCHAR(20) NOT NULL DEFAULT 'NONE',
    `vip_expire_time` DATETIME,
    `token_version` INT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE (`phone`)
);

CREATE TABLE `trainer_application` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `real_name` VARCHAR(50) NOT NULL,
    `certification_photos` JSON,
    `bio` VARCHAR(500),
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    `review_comment` VARCHAR(500),
    `reviewed_by` BIGINT,
    `reviewed_at` DATETIME,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `course` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `publisher_id` BIGINT NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `type` VARCHAR(20) NOT NULL,
    `description` TEXT,
    `cover_image` VARCHAR(500),
    `video_url` VARCHAR(500),
    `duration` INT NOT NULL,
    `max_capacity` INT,
    `is_vip_only` TINYINT NOT NULL DEFAULT 0,
    `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    `audit_comment` VARCHAR(500),
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
