package com.example.backend.service;

import com.example.backend.model.dto.dingtalkAttendanceRecord.DingtalkAttendanceRecordQueryRequest;
import com.example.backend.model.dto.dingtalkAttendanceRecord.DingtalkAttendanceRecordUpdateRequest;
import com.example.backend.model.entity.DingtalkAttendanceRecord;
import com.example.backend.model.vo.dingtalkAttendanceRecord.DingtalkAttendanceRecordVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 钉钉考勤记录服务层。
 *
 * @author <a href="https://github.com/yyffyyq">代码制造者yfy</a>
 */
public interface DingtalkAttendanceRecordService extends IService<DingtalkAttendanceRecord> {

    /**
     * 【用户】获取当前用户的考勤记录
     * @param request HTTP请求
     * @param queryDate 查询日期（默认为今天）
     * @return 考勤记录列表
     */
    List<DingtalkAttendanceRecordVO> getMyAttendanceRecords(HttpServletRequest request, LocalDate queryDate);

    /**
     * 【管理员】根据用户ID获取考勤记录
     * @param userId 用户ID
     * @param queryDate 查询日期（默认为今天）
     * @return 考勤记录列表
     */
    List<DingtalkAttendanceRecordVO> getAttendanceRecordsByUserId(String userId, LocalDate queryDate,HttpServletRequest request);

    /**
     * 【管理员】根据考勤组ID更新考勤记录（从钉钉API获取并保存）
     * @param dingtalkAttendanceRecordUpdateRequest 更新钉钉考勤组信息请求
     * @param request HTTP请求
     * @return 更新的记录数量
     */
    Integer syncAttendanceRecordsByGroupId(DingtalkAttendanceRecordUpdateRequest dingtalkAttendanceRecordUpdateRequest, HttpServletRequest request);

    /**
     * 【定时任务】根据考勤组ID更新考勤记录（从钉钉API获取并保存）
     * @param groupId 考勤组ID
     * @param checkDateFrom 开始时间
     * @param checkDateTo 结束时间
     * @param accessToken 钉钉AccessToken
     * @return 更新的记录数量
     */
    Integer syncAttendanceRecordsByGroupId(String groupId, LocalDateTime checkDateFrom, LocalDateTime checkDateTo, String accessToken);

    /**
     * 【管理员】分页查询考勤组内所有人员的考勤记录
     * @param queryRequest 查询请求
     * @return 分页考勤记录
     */
    Page<DingtalkAttendanceRecordVO> getAttendanceRecordsByGroupId(DingtalkAttendanceRecordQueryRequest queryRequest);

    /**
     * 构建查询条件
     * @param queryRequest 查询请求
     * @return QueryWrapper
     */
    QueryWrapper getQueryWrapper(DingtalkAttendanceRecordQueryRequest queryRequest);

    /**
     * 获取封装后的考勤记录VO
     * @param record 考勤记录实体
     * @return 考勤记录VO
     */
    DingtalkAttendanceRecordVO getDingtalkAttendanceRecordVO(DingtalkAttendanceRecord record);

    /**
     * 获取封装后的考勤记录VO列表
     * @param records 考勤记录实体列表
     * @return 考勤记录VO列表
     */
    List<DingtalkAttendanceRecordVO> getDingtalkAttendanceRecordVOList(List<DingtalkAttendanceRecord> records);

    /**
     * 更新is_legal 和 is_narmal 信息
     * （ is_legal 信息为 null 的考勤信息）
     * @return 是否更新成功状态
     */
    boolean updateIsLegalAndIsNormal();

    /**
     * 【用户/管理员】根据用户ID和月份获取考勤记录列表
     * @param userId 用户ID
     * @param month 月份（格式：yyyy-MM）
     * @param request HTTP请求
     * @return 考勤记录列表
     */
    List<DingtalkAttendanceRecordVO> getAttendanceRecordsByUserIdAndMonth(String userId, String month, HttpServletRequest request);
}
