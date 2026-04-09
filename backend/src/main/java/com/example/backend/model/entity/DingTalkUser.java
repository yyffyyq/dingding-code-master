package com.example.backend.model.entity;

import lombok.Data;

/**
 * 钉钉用户实体类（只存储钉钉返回的核心字段）
 */
@Data
public class DingTalkUser {
    // 钉钉用户ID
    private String userid;
    // 姓名
    private String name;
    // 手机号
    private String mobile;
    // 头像
    private String avatar;
    // 部门ID列表
    private Long deptId;
    // 职位
    private String title;
    // 邮箱
    private String email;
}