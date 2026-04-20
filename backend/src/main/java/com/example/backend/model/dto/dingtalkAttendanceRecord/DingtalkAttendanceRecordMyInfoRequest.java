package com.example.backend.model.dto.dingtalkAttendanceRecord;

import lombok.Data;

import java.io.Serializable;

/**
 * 查看当前用户考勤情况请求
 */

@Data
public class DingtalkAttendanceRecordMyInfoRequest implements Serializable {
    /**
     * 用户id
     */
    private String userId;
    /**
     * 月份信息
     */
    private String month;

}

