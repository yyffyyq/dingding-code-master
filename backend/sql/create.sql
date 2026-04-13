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

-- 钉钉考勤打卡记录表
CREATE TABLE `dingtalk_attendance_record` (
                                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                              `record_id` varchar(64) NOT NULL COMMENT '钉钉原始打卡记录ID（唯一）',
                                              `user_id` varchar(64) NOT NULL COMMENT '员工ID（关联user_kaoqin表）',
                                              `group_id` VARCHAR(64) NOT NULL COMMENT '考勤组ID（关联group_kaoqin表）',
                                              `work_date` date NOT NULL COMMENT '打卡日期（yyyy-MM-dd）',
                                              `check_type` varchar(20) NOT NULL COMMENT '打卡类型：OnDuty=上班, OffDuty=下班',
                                              `check_time` datetime NOT NULL COMMENT '实际打卡时间',
                                              `time_result` varchar(20) NOT NULL COMMENT '时间结果：Normal=正常',
                                              `location_result` varchar(20) NOT NULL COMMENT '位置结果：Normal=正常',
                                              `is_normal` tinyint NOT NULL DEFAULT 0 COMMENT '是否正常打卡：1=是,0=否',
                                              `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                              `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                              `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除：0=未删除,1=已删除',
                                              PRIMARY KEY (`id`),
                                              UNIQUE KEY `uk_record_id` (`record_id`) USING BTREE COMMENT '防止重复导入打卡记录',
                                              KEY `idx_user_date` (`user_id`,`work_date`) USING BTREE COMMENT '按员工+日期快速查询考勤'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钉钉考勤打卡记录表';


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

-- 创建用户id与考勤组id关联表
CREATE TABLE `user_group_kaoqin_rel` (
                                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                         `user_id` VARCHAR(100) NOT NULL COMMENT '关联user_kaoqin表的用户ID',
                                         `group_id` VARCHAR(100) NOT NULL COMMENT '关联group_kaoqin表的考勤组ID',
                                         `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                         `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除 0-未删除 1-已删除',
                                         PRIMARY KEY (`id`),
    -- 唯一索引：防止同一个用户重复绑定同一个考勤组（核心业务约束）
                                         UNIQUE KEY `uk_user_group` (`user_id`,`group_id`,`is_deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-考勤组关联表';
