package com.example.backend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.example.backend.exception.BusinessException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.exception.ThrowUtils;
import com.example.backend.mapper.DingtalkAttendanceRecordMapper;
import com.example.backend.mapper.UserGroupKaoqinRelMapper;
import com.example.backend.mapper.UserKaoqinMapper;
import com.example.backend.model.dto.dingtalkAttendanceRecord.DingtalkAttendanceRecordQueryRequest;
import com.example.backend.model.dto.dingtalkAttendanceRecord.DingtalkAttendanceRecordUpdateRequest;
import com.example.backend.model.entity.DingtalkAttendanceRecord;
import com.example.backend.model.entity.SysUser;
import com.example.backend.model.entity.UserGroupKaoqinRel;
import com.example.backend.model.entity.UserKaoqin;
import com.example.backend.model.vo.dingtalkAttendanceRecord.DingtalkAttendanceRecordVO;
import com.example.backend.service.DingtalkAttendanceRecordService;
import com.example.backend.service.SysUserService;
import com.example.backend.service.UserGroupKaoqinRelService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.backend.constant.CommonConstant.ACCSEE_TOKEN;
import static com.example.backend.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 钉钉考勤记录服务层实现。
 *
 * @author <a href="https://github.com/yyffyyq">代码制造者yfy</a>
 */
@Slf4j
@Service
public class DingtalkAttendanceRecordServiceImpl extends ServiceImpl<DingtalkAttendanceRecordMapper, DingtalkAttendanceRecord> implements DingtalkAttendanceRecordService {

    @Resource
    private DingtalkAttendanceRecordMapper dingtalkAttendanceRecordMapper;

    @Resource
    private UserGroupKaoqinRelMapper userGroupKaoqinRelMapper;

    @Resource
    private UserKaoqinMapper userKaoqinMapper;

    @Resource
    private SysUserService sysUserService;

    @Resource
    private UserGroupKaoqinRelService userGroupKaoqinRelService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${dingtalk.attendanceListRecord:https://oapi.dingtalk.com/attendance/list}")
    private String attendanceListRecordUrl;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 【用户】获取当前用户的考勤记录
     *
     * @param request   HTTP请求
     * @param queryDate 查询日期（默认为今天）
     * @return 考勤记录列表
     */
    @Override
    public List<DingtalkAttendanceRecordVO> getMyAttendanceRecords(HttpServletRequest request, LocalDate queryDate) {
        // 1. 获取当前登录用户
        SysUser loginUser = sysUserService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        // 2. 如果没有指定日期，默认为今天
        if (queryDate == null) {
            queryDate = LocalDate.now();
        }

        // 3. 查询考勤记录
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(DingtalkAttendanceRecord::getUserId, loginUser.getUserId())
                .eq(DingtalkAttendanceRecord::getWorkDate, Date.valueOf(queryDate))
                .eq(DingtalkAttendanceRecord::getIsDeleted, false)
                .orderBy(DingtalkAttendanceRecord::getUserCheckTime, true);

        List<DingtalkAttendanceRecord> records = dingtalkAttendanceRecordMapper.selectListByQuery(queryWrapper);

        log.info("获取当前用户的考勤记录"+"操作人员："+request.getSession().getAttribute(USER_LOGIN_STATE)+"；查询信息:"+records);

        return getDingtalkAttendanceRecordVOList(records);
    }

    /**
     * 【管理员】根据用户ID获取考勤记录
     *
     * @param userId    用户ID
     * @param queryDate 查询日期（默认为今天）
     * @return 考勤记录列表
     */
    @Override
    public List<DingtalkAttendanceRecordVO> getAttendanceRecordsByUserId(String userId, LocalDate queryDate,HttpServletRequest request) {
        // 1. 参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(userId), ErrorCode.PARAMS_ERROR, "用户ID不能为空");

        // 2. 如果没有指定日期，默认为今天
        if (queryDate == null) {
            queryDate = LocalDate.now();
        }

        // 3. 查询考勤记录
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(DingtalkAttendanceRecord::getUserId, userId)
                .eq(DingtalkAttendanceRecord::getWorkDate, Date.valueOf(queryDate))
                .eq(DingtalkAttendanceRecord::getIsDeleted, false)
                .orderBy(DingtalkAttendanceRecord::getUserCheckTime, true);

        List<DingtalkAttendanceRecord> records = dingtalkAttendanceRecordMapper.selectListByQuery(queryWrapper);

        log.info("根据用户ID获取考勤记录"+"操作人员："+request.getSession().getAttribute(USER_LOGIN_STATE)+"；查询信息:"+records);

        return getDingtalkAttendanceRecordVOList(records);
    }

    /**
     * 【管理员】根据考勤组ID更新考勤记录（从钉钉API获取并保存）
     * 自动获取昨天的考勤信息（以2:00为第二天计算）
     *
     * @param dingtalkAttendanceRecordUpdateRequest 更新钉钉考勤组信息请求
     * @param request HTTP请求
     * @return 更新的记录数量
     */
    @Override
    public Integer syncAttendanceRecordsByGroupId(DingtalkAttendanceRecordUpdateRequest dingtalkAttendanceRecordUpdateRequest, HttpServletRequest request) {
        // 1. 参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(dingtalkAttendanceRecordUpdateRequest.getGroupId()), ErrorCode.PARAMS_ERROR, "考勤组ID不能为空");
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        String groupId = dingtalkAttendanceRecordUpdateRequest.getGroupId();

        // 2. 获取access_token
        Object accessTokenObj = request.getSession().getAttribute(ACCSEE_TOKEN);
        String accessToken = (String) accessTokenObj;
        ThrowUtils.throwIf(StrUtil.isBlank(accessToken), ErrorCode.PARAMS_ERROR, "AccessToken不能为空");

        // 3. 根据考勤组ID获取用户ID列表
        List<String> userIdList = userGroupKaoqinRelService.getIdListByGroupId(groupId);
        ThrowUtils.throwIf(CollUtil.isEmpty(userIdList), ErrorCode.PARAMS_ERROR, "考勤组内没有用户");

        // 4. 计算查询时间范围（获取昨天的考勤信息，以2:00为第二天计算）
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime checkDateFrom;
        LocalDateTime checkDateTo;

        if (now.getHour() < 2) {
            // 如果当前时间在2:00之前，昨天是前天到昨天
            checkDateFrom = now.minusDays(2).withHour(0).withMinute(0).withSecond(0);
            checkDateTo = now.minusDays(1).withHour(0).withMinute(0).withSecond(0);
        } else {
            // 如果当前时间在2:00之后，昨天是昨天到今天
            checkDateFrom = now.minusDays(1).withHour(0).withMinute(0).withSecond(0);
            checkDateTo = now.withHour(0).withMinute(0).withSecond(0);
        }

        // 5. 调用钉钉API获取考勤记录
        List<DingtalkAttendanceRecord> records = new ArrayList<>();
        if (dingtalkAttendanceRecordUpdateRequest.getCheckDateFrom() == null ||
                dingtalkAttendanceRecordUpdateRequest.getCheckDateTo() == null) {
            records = fetchAttendanceRecordsFromDingTalk(userIdList, checkDateFrom, checkDateTo, accessToken);
        }else{
                    records = fetchAttendanceRecordsFromDingTalk(userIdList, dingtalkAttendanceRecordUpdateRequest.getCheckDateFrom()
                    , dingtalkAttendanceRecordUpdateRequest.getCheckDateTo()
                    , accessToken);
        }

        // 6. 保存考勤记录到数据库
        Integer savedCount = saveAttendanceRecords(records, groupId);

        log.info("根据考勤组ID更新考勤记录"+"操作人员："+request.getSession().getAttribute(USER_LOGIN_STATE)+"；查询信息:"+
                records+"成功同步考勤记录，考勤组ID：{}，记录数：{}", groupId, savedCount);
        return savedCount;
    }

    /**
     * 【定时任务】根据考勤组ID更新考勤记录（从钉钉API获取并保存）
     *
     * @param groupId 考勤组ID
     * @param checkDateFrom 开始时间
     * @param checkDateTo 结束时间
     * @param accessToken 钉钉AccessToken
     * @return 更新的记录数量
     */
    @Override
    public Integer syncAttendanceRecordsByGroupId(String groupId, LocalDateTime checkDateFrom, LocalDateTime checkDateTo, String accessToken) {
        // 1. 参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(groupId), ErrorCode.PARAMS_ERROR, "考勤组ID不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(accessToken), ErrorCode.PARAMS_ERROR, "AccessToken不能为空");

        // 2. 根据考勤组ID获取用户ID列表
        List<String> userIdList = userGroupKaoqinRelService.getIdListByGroupId(groupId);
        ThrowUtils.throwIf(CollUtil.isEmpty(userIdList), ErrorCode.PARAMS_ERROR, "考勤组内没有用户");

        // 3. 调用钉钉API获取考勤记录
        List<DingtalkAttendanceRecord> records = fetchAttendanceRecordsFromDingTalk(userIdList, checkDateFrom, checkDateTo, accessToken);

        // 4. 保存考勤记录到数据库
        Integer savedCount = saveAttendanceRecords(records, groupId);

        log.info("【定时任务】成功同步考勤记录，考勤组ID：{}，记录数：{}", groupId, savedCount);
        return savedCount;
    }

    /**
     * 从钉钉API获取考勤记录
     *
     * @param userIdList    用户ID列表
     * @param checkDateFrom 开始时间
     * @param checkDateTo   结束时间
     * @param accessToken   访问令牌
     * @return 考勤记录列表
     */

    // todo 需要添加 offset
    //  表示获取考勤数据的起始点。第一次传0，如果还有多余数据，下次获取传的offset值为之前的offset+limit，0、1、2...依次递增。
    private List<DingtalkAttendanceRecord> fetchAttendanceRecordsFromDingTalk(List<String> userIdList,
                                                                               LocalDateTime checkDateFrom,
                                                                               LocalDateTime checkDateTo,
                                                                               String accessToken) {
        List<DingtalkAttendanceRecord> resultList = new ArrayList<>();

        try {
            String dingAPI = attendanceListRecordUrl + "?access_token=" + accessToken;

            // 钉钉API一次最多查询50个用户
            int batchSize = 50;
            for (int i = 0; i < userIdList.size(); i += batchSize) {
                List<String> batchUserIds = userIdList.subList(i, Math.min(i + batchSize, userIdList.size()));

                int offset = 0;
                int limit = 50;
                boolean hasMore = true;

                while (hasMore){
                    // 构建请求体
                    Map<String, Object> requestBody = new HashMap<>();
                    requestBody.put("workDateFrom", checkDateFrom.format(DATE_TIME_FORMATTER));
                    // todo 这里传入的值需要根据返回值hasMore判断是否查询完
                    requestBody.put("offset", offset);
                    requestBody.put("userIdList", batchUserIds);
                    requestBody.put("limit", limit);
                    requestBody.put("isI18n", false);
                    requestBody.put("workDateTo", checkDateTo.format(DATE_TIME_FORMATTER));

                    // 调用钉钉API
                    ResponseEntity<String> responseEntity = restTemplate.postForEntity(dingAPI, requestBody, String.class);
                    String rawJsonResult = responseEntity.getBody();

                    // 解析响应
                    JsonNode rootNode = objectMapper.readTree(rawJsonResult);
                    int errcode = rootNode.path("errcode").asInt();
                    // 判断是否需要继续查询的依据
                    hasMore = rootNode.path("hasMore").asBoolean();

                    if (errcode != 0) {
                        String errmsg = rootNode.path("errmsg").asText();
                        log.error("钉钉API调用失败：{}", errmsg);
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "钉钉API调用失败：" + errmsg);
                    }

                    // 解析考勤记录
                    JsonNode recordList = rootNode.path("recordresult");
                    if (recordList.isArray()) {
                        for (JsonNode recordNode : recordList) {
                            DingtalkAttendanceRecord record = parseAttendanceRecord(recordNode);
                            if (record != null) {
                                resultList.add(record);
                            }
                        }
                    }
                    // 进入下一页
                    offset += limit;
                }
            }
        } catch (Exception e) {
            log.error("获取钉钉考勤记录失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取钉钉考勤记录失败：" + e.getMessage());
        }

        return resultList;
    }

    /**
     * 解析钉钉考勤记录
     *
     * @param recordNode JSON节点
     * @return 考勤记录实体
     */
    private DingtalkAttendanceRecord parseAttendanceRecord(JsonNode recordNode) {
        try {
            DingtalkAttendanceRecord record = new DingtalkAttendanceRecord();

            // ===== 基础字段 =====
            record.setRecordId(
                    recordNode.has("recordId")
                            ? recordNode.path("recordId").asText()
                            : recordNode.path("id").asText()
            );

            record.setCorpId(recordNode.path("corpId").asText(null));
            record.setUserId(recordNode.path("userId").asText(null));
            record.setGroupId(recordNode.path("groupId").asText(null));
            record.setCheckType(recordNode.path("checkType").asText(null));

            // ===== workDate（毫秒时间戳 → Date）=====
            long workDateMillis = recordNode.path("workDate").asLong();
            if (workDateMillis > 0) {
                record.setWorkDate(new Date(workDateMillis));
            }

            // ===== 时间字段 =====
            record.setUserCheckTime(parseTime(recordNode, "userCheckTime"));
            record.setPlanCheckTime(parseTime(recordNode, "planCheckTime"));
            record.setBaseCheckTime(parseTime(recordNode, "baseCheckTime"));

            // ===== 排班 =====
            if (recordNode.has("classId")) {
                record.setClassId(recordNode.path("classId").asLong());
            }

            if (recordNode.has("planId")) {
                record.setPlanId(recordNode.path("planId").asLong());
            }

            // ===== 结果 =====
            record.setTimeResult(recordNode.path("timeResult").asText(null));
            record.setLocationResult(recordNode.path("locationResult").asText(null));

            String isLegal = recordNode.path("isLegal").asText(null);
            record.setIsLegal(isLegal);
            record.setIsNormal("Normal".equalsIgnoreCase(isLegal));

            // ===== 来源 =====
            record.setSourceType(recordNode.path("sourceType").asText(null));
            record.setLocationMethod(recordNode.path("locationMethod").asText(null));

            // ===== 钉钉时间 =====
            record.setDingCreateTime(parseTime(recordNode, "gmtCreate"));
            record.setDingModifyTime(parseTime(recordNode, "gmtModified"));

            // ===== 原始数据 =====
            record.setRawJson(recordNode.toString());

            // ===== 系统字段 =====
            record.setCreateTime(LocalDateTime.now());
            record.setUpdateTime(LocalDateTime.now());
            record.setIsDeleted(false);

            return record;

        } catch (Exception e) {
            log.error("解析考勤记录失败: {}", recordNode, e);
            return null;
        }
    }

    private LocalDateTime parseTime(JsonNode node, String fieldName) {
        long millis = node.path(fieldName).asLong();
        if (millis <= 0) {
            return null;
        }
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(millis),
                ZoneId.systemDefault()
        );
    }

    /**
     * 保存考勤记录到数据库
     *
     * @param records 考勤记录列表
     * @param groupId 考勤组ID
     * @return 保存的记录数量
     */
    private Integer saveAttendanceRecords(List<DingtalkAttendanceRecord> records, String groupId) {
        if (CollUtil.isEmpty(records)) {
            return 0;
        }

        // 获取已存在的记录ID
        Set<String> existingRecordIds = getExistingRecordIdSet();

        // 过滤出需要新增的记录
        List<DingtalkAttendanceRecord> insertList = records.stream()
                .filter(record -> !existingRecordIds.contains(record.getRecordId()))
                .collect(Collectors.toList());

        // 批量插入
        if (CollUtil.isNotEmpty(insertList)) {
            dingtalkAttendanceRecordMapper.insertBatchList(insertList);
            log.info("批量插入考勤记录{}条", insertList.size());
        }

        return insertList.size();
    }

    /**
     * 获取已存在的记录ID集合
     *
     * @return 记录ID集合
     */
    private Set<String> getExistingRecordIdSet() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(DingtalkAttendanceRecord::getRecordId);
        List<String> recordIdList = dingtalkAttendanceRecordMapper.selectObjectListByQueryAs(queryWrapper, String.class);
        return new HashSet<>(recordIdList);
    }

    /**
     * 【管理员】分页查询考勤组内所有人员的考勤记录
     *
     * @param queryRequest 查询请求
     * @return 分页考勤记录
     */
    @Override
    public Page<DingtalkAttendanceRecordVO> getAttendanceRecordsByGroupId(DingtalkAttendanceRecordQueryRequest queryRequest) {
        // 1. 参数校验
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(queryRequest.getGroupId()), ErrorCode.PARAMS_ERROR, "考勤组ID不能为空");

        long pageNum = queryRequest.getPageNum();
        long pageSize = queryRequest.getPageSize();

        // 2. 如果没有指定日期，默认为今天
        if (queryRequest.getQueryDate() == null) {
            queryRequest.setQueryDate(LocalDate.now());
        }

        // 3. 根据考勤组ID获取用户ID列表
        List<String> userIdList = userGroupKaoqinRelService.getIdListByGroupId(queryRequest.getGroupId());

        // 4. 如果考勤组内没有用户，返回空分页
        Page<DingtalkAttendanceRecordVO> emptyPage = new Page<>(pageNum, pageSize, 0);
        if (CollUtil.isEmpty(userIdList)) {
            emptyPage.setRecords(new ArrayList<>());
            return emptyPage;
        }

        // 5. 构建查询条件并分页查询
        QueryWrapper queryWrapper = getQueryWrapper(queryRequest);
        queryWrapper.in(DingtalkAttendanceRecord::getUserId, userIdList);

        Page<DingtalkAttendanceRecord> recordPage = dingtalkAttendanceRecordMapper.paginate(pageNum, pageSize, queryWrapper);

        // 6. 封装返回结果
        Page<DingtalkAttendanceRecordVO> voPage = new Page<>(pageNum, pageSize, recordPage.getTotalRow());
        List<DingtalkAttendanceRecordVO> voList = getDingtalkAttendanceRecordVOList(recordPage.getRecords());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 构建查询条件
     *
     * @param queryRequest 查询请求
     * @return QueryWrapper
     */
    @Override
    public QueryWrapper getQueryWrapper(DingtalkAttendanceRecordQueryRequest queryRequest) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR, "查询请求参数为空");

        QueryWrapper queryWrapper = QueryWrapper.create();

        String userId = queryRequest.getUserId();
        String groupId = queryRequest.getGroupId();
        LocalDate queryDate = queryRequest.getQueryDate();
        String checkType = queryRequest.getCheckType();
        String timeResult = queryRequest.getTimeResult();
        String locationResult = queryRequest.getLocationResult();
        String sortField = queryRequest.getSortField();
        String sortOrder = queryRequest.getSortOrder();

        // 添加查询条件
        queryWrapper.eq(DingtalkAttendanceRecord::getUserId, userId, StrUtil.isNotBlank(userId));
        queryWrapper.eq(DingtalkAttendanceRecord::getGroupId, groupId, StrUtil.isNotBlank(groupId));
        queryWrapper.eq(DingtalkAttendanceRecord::getWorkDate, queryDate != null ? Date.valueOf(queryDate) : null, queryDate != null);
        queryWrapper.eq(DingtalkAttendanceRecord::getCheckType, checkType, StrUtil.isNotBlank(checkType));
        queryWrapper.eq(DingtalkAttendanceRecord::getTimeResult, timeResult, StrUtil.isNotBlank(timeResult));
        queryWrapper.eq(DingtalkAttendanceRecord::getLocationResult, locationResult, StrUtil.isNotBlank(locationResult));
        queryWrapper.eq(DingtalkAttendanceRecord::getIsDeleted, false);

        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            queryWrapper.orderBy(DingtalkAttendanceRecord::getUserCheckTime, false);
        }

        return queryWrapper;
    }

    /**
     * 获取封装后的考勤记录VO
     *
     * @param record 考勤记录实体
     * @return 考勤记录VO
     */
    @Override
    public DingtalkAttendanceRecordVO getDingtalkAttendanceRecordVO(DingtalkAttendanceRecord record) {
        if (record == null) {
            return null;
        }

        DingtalkAttendanceRecordVO vo = new DingtalkAttendanceRecordVO();
        BeanUtils.copyProperties(record, vo);

        // 查询用户名字
        if (StrUtil.isNotBlank(record.getUserId())) {
            QueryWrapper userQuery = QueryWrapper.create()
                    .select(UserKaoqin::getUserName)
                    .eq(UserKaoqin::getUserId, record.getUserId());
            String userName = userKaoqinMapper.selectObjectByQueryAs(userQuery, String.class);
            vo.setUserName(userName);
        }

        return vo;
    }

    /**
     * 获取封装后的考勤记录VO列表
     *
     * @param records 考勤记录实体列表
     * @return 考勤记录VO列表
     */
    @Override
    public List<DingtalkAttendanceRecordVO> getDingtalkAttendanceRecordVOList(List<DingtalkAttendanceRecord> records) {
        if (CollUtil.isEmpty(records)) {
            return new ArrayList<>();
        }

        return records.stream()
                .map(this::getDingtalkAttendanceRecordVO)
                .collect(Collectors.toList());
    }
}
