package com.example.backend.model.dto.dingtalkAttendanceRecord;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class DingtalkAttendanceRecordUpdateRequest implements Serializable {

    /**
     * 考勤组id
     */
    private String groupId;
    /**
     * 查看考勤日期-开始
     */
    private LocalDateTime checkDateFrom;
    /**
     * 查看考勤日期-结束
     */
    private LocalDateTime checkDateTo;
}
