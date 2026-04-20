package com.example.backend.controller;

import com.example.backend.annotion.AuthCheck;
import com.example.backend.common.BaseResponse;
import com.example.backend.common.ResultUtils;
import com.example.backend.exception.ErrorCode;
import com.example.backend.exception.ThrowUtils;
import com.example.backend.model.dto.dingtalkAttendanceRecord.DingtalkAttendanceRecordQueryRequest;
import com.example.backend.model.dto.dingtalkAttendanceRecord.DingtalkAttendanceRecordUpdateRequest;
import com.example.backend.model.vo.dingtalkAttendanceRecord.DingtalkAttendanceRecordVO;
import com.example.backend.service.DingtalkAttendanceRecordService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static com.example.backend.constant.UserConstant.ADMIN_ROLE;

/**
 * 钉钉考勤记录控制层。
 *
 * @author <a href="https://github.com/yyffyyq">代码制造者yfy</a>
 */
@RestController
@RequestMapping("/dingtalkAttendanceRecord")
//@Tag(name = "钉钉考勤记录管理")
public class DingtalkAttendanceRecordController {

    @Autowired
    private DingtalkAttendanceRecordService dingtalkAttendanceRecordService;

    /**
     * 【用户】获取当前用户的考勤记录
     *
     * @param queryDate 查询日期（格式：yyyy-MM-dd，默认为今天）
     * @param request   HTTP请求
     * @return 考勤记录列表
     */
    @GetMapping("/my/records")
    @Operation(summary = "获取我的考勤记录", description = "当前用户查看自己的考勤情况，默认查询今天")
    public BaseResponse<List<DingtalkAttendanceRecordVO>> getMyAttendanceRecords(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate queryDate,
            HttpServletRequest request) {

        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);

        List<DingtalkAttendanceRecordVO> records = dingtalkAttendanceRecordService.getMyAttendanceRecords(request, queryDate);

        return ResultUtils.success(records);
    }

    /**
     * 【管理员】根据用户ID获取考勤记录
     *
     * @param userId    用户ID
     * @param queryDate 查询日期（格式：yyyy-MM-dd，默认为今天）
     * @return 考勤记录列表
     */
    @GetMapping("/user/{userId}")
    @AuthCheck(mustRole = ADMIN_ROLE)
    @Operation(summary = "获取指定用户的考勤记录", description = "管理员查看单个考勤人员的考勤情况，默认查询今天")
    public BaseResponse<List<DingtalkAttendanceRecordVO>> getAttendanceRecordsByUserId(
            @PathVariable String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate queryDate,
            HttpServletRequest request) {

        ThrowUtils.throwIf(userId == null || userId.isEmpty(), ErrorCode.PARAMS_ERROR, "用户ID不能为空");

        List<DingtalkAttendanceRecordVO> records = dingtalkAttendanceRecordService.getAttendanceRecordsByUserId(userId, queryDate, request);

        return ResultUtils.success(records);
    }

    /**
     * 【管理员】根据考勤组ID同步考勤记录
     *
     * @param dingtalkAttendanceRecordUpdateRequest 考勤组ID
     * @param request HTTP请求
     * @return 同步的记录数量
     */
    @PostMapping("/sync/group")
    @AuthCheck(mustRole = ADMIN_ROLE)
    @Operation(summary = "同步考勤组考勤记录", description = "根据考勤组ID从钉钉API获取并更新考勤记录，自动获取昨天的考勤信息（以2:00为第二天计算）")
    public BaseResponse<Integer> syncAttendanceRecordsByGroupId(
            @RequestBody DingtalkAttendanceRecordUpdateRequest dingtalkAttendanceRecordUpdateRequest,
            HttpServletRequest request) {

        ThrowUtils.throwIf(dingtalkAttendanceRecordUpdateRequest == null || dingtalkAttendanceRecordUpdateRequest.getGroupId().isEmpty(), ErrorCode.PARAMS_ERROR, "考勤组ID不能为空");
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);

        Integer count = dingtalkAttendanceRecordService.syncAttendanceRecordsByGroupId(dingtalkAttendanceRecordUpdateRequest, request);

        // todo 创建一个对考勤信息 is_legal 是否合法记录：Y/N is_normal 是否正常打卡：1=正常 两个值做一个批量的判断并存入数据库中，
        //  提取记录的条件为is_legal 为null的记录
        boolean updateResult = dingtalkAttendanceRecordService.updateIsLegalAndIsNormal();

        return ResultUtils.success(count);
    }

    /**
     * 【管理员】分页查询考勤组内所有人员的考勤记录
     *
     * @param queryRequest 查询请求
     * @return 分页考勤记录
     */
    @PostMapping("/group/list")
    @AuthCheck(mustRole = ADMIN_ROLE)
    @Operation(summary = "分页查询考勤组考勤记录", description = "查看当日考勤组内所有考勤人员的考勤信息，默认查询今天")
    public BaseResponse<Page<DingtalkAttendanceRecordVO>> getAttendanceRecordsByGroupId(
            @RequestBody DingtalkAttendanceRecordQueryRequest queryRequest) {

        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(queryRequest.getGroupId() == null || queryRequest.getGroupId().isEmpty(),
                ErrorCode.PARAMS_ERROR, "考勤组ID不能为空");

        Page<DingtalkAttendanceRecordVO> page = dingtalkAttendanceRecordService.getAttendanceRecordsByGroupId(queryRequest);
        return ResultUtils.success(page);
    }

}
