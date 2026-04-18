package com.example.backend.model.dto.dingtalkAttendanceRecord;

import com.example.backend.common.PageRequest;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 钉钉考勤记录查询请求
 */
@Data
public class DingtalkAttendanceRecordQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID（钉钉userId）
     */
    private String userId;

    /**
     * 考勤组ID
     */
    private String groupId;

    /**
     * 查询日期（默认为今天）
     */
    private LocalDate queryDate;

    /**
     * 用户名字（模糊查询）
     */
    private String userName;

    /**
     * 打卡类型：OnDuty=上班, OffDuty=下班
     */
    private String checkType;

    /**
     * 时间结果：Normal/Late/Early/Absent
     */
    private String timeResult;

    /**
     * 位置结果：Normal/Outside
     */
    private String locationResult;
}
