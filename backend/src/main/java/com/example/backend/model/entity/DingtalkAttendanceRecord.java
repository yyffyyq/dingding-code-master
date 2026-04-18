package com.example.backend.model.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.sql.Date;
import java.time.LocalDateTime;

import java.io.Serial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  实体类。
 *
 * @author <a href="https://github.com/yyffyyq">代码制造者yfy</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("dingtalk_attendance_record")
public class DingtalkAttendanceRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 钉钉打卡记录ID（唯一）
     */
    private String recordId;

    /**
     * 企业corpId
     */
    private String corpId;

    /**
     * 业务ID（审批/补卡关联）
     */
    private String bizId;

    /**
     * 员工userId
     */
    private String userId;

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
     * 基准打卡时间
     */
    private LocalDateTime baseCheckTime;

    /**
     * 班次ID
     */
    private Long classId;

    /**
     * 排班计划ID
     */
    private Long planId;

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
     * 钉钉创建时间
     */
    private LocalDateTime dingCreateTime;

    /**
     * 钉钉修改时间
     */
    private LocalDateTime dingModifyTime;

    /**
     * 钉钉原始返回JSON
     */
    private String rawJson;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 软删除
     */
    private Boolean isDeleted;

}
