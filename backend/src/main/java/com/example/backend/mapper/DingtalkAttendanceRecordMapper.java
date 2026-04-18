package com.example.backend.mapper;

import com.example.backend.model.entity.DingtalkAttendanceRecord;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 钉钉考勤记录映射层。
 *
 * @author <a href="https://github.com/yyffyyq">代码制造者yfy</a>
 */
public interface DingtalkAttendanceRecordMapper extends BaseMapper<DingtalkAttendanceRecord> {

    /**
     * 批量插入考勤记录
     * @param records 考勤记录列表
     * @return 插入的记录数
     */
    int insertBatchList(@Param("list") List<DingtalkAttendanceRecord> records);

    /**
     * 根据记录ID批量查询
     * @param recordIds 记录ID列表
     * @return 考勤记录列表
     */
    List<DingtalkAttendanceRecord> selectByRecordIds(@Param("recordIds") List<String> recordIds);

    /**
     * 根据用户ID和日期范围查询考勤记录
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 考勤记录列表
     */
    List<DingtalkAttendanceRecord> selectByUserIdAndDateRange(@Param("userId") String userId,
                                                               @Param("startDate") String startDate,
                                                               @Param("endDate") String endDate);
}
