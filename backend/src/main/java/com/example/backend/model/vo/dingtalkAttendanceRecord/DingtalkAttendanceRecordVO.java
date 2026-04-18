package com.example.backend.model.vo.dingtalkAttendanceRecord;

import lombok.Data;

import java.io.Serializable;
import java.sql.Date;
import java.time.LocalDateTime;

/**
 * 钉钉考勤记录视图对象
 */
@Data
public class DingtalkAttendanceRecordVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 钉钉打卡记录ID（唯一）
     */
    private String recordId;

    /**
     * 员工userId
     */
    private String userId;

    /**
     * 用户名字
     */
    private String userName;

    /**
     * 考勤组ID
     */
    private String groupId;

    /**
     * 工作日期（yyyy-MM-dd）
     */
    private Date workDate;

    /**
     * 打卡类型：OnDuty=上班, OffDuty=下班
     */
    private String checkType;

    /**
     * 实际打卡时间
     */
    private LocalDateTime userCheckTime;

    /**
     * 计划打卡时间
     */
    private LocalDateTime planCheckTime;

    /**
     * 时间结果：Normal/Late/Early/Absent
     */
    private String timeResult;

    /**
     * 位置结果：Normal/Outside
     */
    private String locationResult;

    /**
     * 是否合法记录：Y/N
     */
    private String isLegal;

    /**
     * 是否正常打卡：1=正常
     */
    private Boolean isNormal;

    /**
     * 打卡来源：USER/BOSS/APPROVE
     */
    private String sourceType;

    /**
     * 定位方式
     */
    private String locationMethod;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
