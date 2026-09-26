DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `trainer_application`;

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
