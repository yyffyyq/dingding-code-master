use kaoqindb;
CREATE TABLE `group_kaoqin` (
                         `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                         `group_id` VARCHAR(64) NOT NULL COMMENT '考勤组编号 (钉钉groupId)',
                         `group_name` VARCHAR(128) DEFAULT NULL COMMENT '考勤组名字',
                         `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         `is_deleted` TINYINT(1) DEFAULT '0' COMMENT '是否删除 (0:正常, 1:已删除)',
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `uk_group_id` (`group_id`) -- 考勤组编号通常是唯一的，加上唯一索引提高查询效率
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤组表';

CREATE TABLE `user_kaoqin` (
                        `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                        `user_id` VARCHAR(64) NOT NULL COMMENT '用户编号 (钉钉userId)',
                        `user_name` VARCHAR(128) DEFAULT NULL COMMENT '用户名字',
                        `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        `is_deleted` TINYINT(1) DEFAULT '0' COMMENT '是否删除 (0:正常, 1:已删除)',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_user_id` (`user_id`) -- 用户编号同样需要唯一索引
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE `attendance_record` (
                                     `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                     `group_id` VARCHAR(64) NOT NULL COMMENT '考勤组编号 (关联 group_kaoqin.group_id)',
                                     `user_id` VARCHAR(64) NOT NULL COMMENT '用户编号 (关联 user_kaoqin.user_id)',
                                     `work_date` DATE NOT NULL COMMENT '排班/工作日日期 (如 2020-09-06)',
                                     `check_time` DATETIME DEFAULT NULL COMMENT '实际打卡时间',
                                     `status` VARCHAR(32) NOT NULL COMMENT '打卡情况 (如: Normal-正常, NotSigned-缺卡, Repaired-补卡等)',
                                     `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录拉取/创建时间',
                                     `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
                                     PRIMARY KEY (`id`),
                                     KEY `idx_user_workdate` (`user_id`, `work_date`), -- 复合索引，方便按人员和日期查询打卡
                                     KEY `idx_group_id` (`group_id`) -- 方便按考勤组统计
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤组成员打卡记录表';


-- 修改关联表attendance外键约束为user_kaoqin里的user_id
ALTER TABLE `attendance_record`
    ADD CONSTRAINT `fk_attendance_user`
        FOREIGN KEY (`user_id`) REFERENCES `user_kaoqin` (`user_id`);
ALTER TABLE `attendance_record`
    ADD CONSTRAINT `fk_attendance_group`
        FOREIGN KEY (`group_id`) REFERENCES `group_kaoqin` (`group_id`);

CREATE TABLE `sys_user` (
                            `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
                            `union_id` VARCHAR(100) NOT NULL COMMENT '钉钉全局唯一标识(跨应用唯一)',
                            `user_id` VARCHAR(100) COMMENT '钉钉企业内员工ID(或系统自定义用户ID)',
                            `nick_name` VARCHAR(50) COMMENT '用户昵称',
                            `user_role` VARCHAR(20) DEFAULT 'user' COMMENT '用户角色：admin-管理员, user-普通用户',
                            `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            UNIQUE KEY `uk_union_id` (`union_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户角色表';
