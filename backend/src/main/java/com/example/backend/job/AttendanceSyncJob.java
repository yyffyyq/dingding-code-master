package com.example.backend.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.example.backend.mapper.GroupKaoqinMapper;
import com.example.backend.model.entity.GroupKaoqin;
import com.example.backend.service.DingUserService;
import com.example.backend.service.DingtalkAttendanceRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 考勤记录定时同步任务
 * 每天凌晨2:00自动执行，同步所有考勤组的考勤记录
 */
@Slf4j
@Component
public class AttendanceSyncJob {

    @Autowired
    private GroupKaoqinMapper groupKaoqinMapper;

    @Autowired
    private DingUserService dingUserService;

    @Autowired
    private DingtalkAttendanceRecordService dingtalkAttendanceRecordService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 定时任务：每天凌晨2:00执行
     * 查询所有考勤组，依次同步每个考勤组的考勤记录
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void syncAllGroupAttendanceRecords() {
        log.info("========== 开始执行定时考勤记录同步任务 ==========");

        try {
            // 1. 获取所有考勤组
            List<GroupKaoqin> groupList = groupKaoqinMapper.selectAll();
            if (CollUtil.isEmpty(groupList)) {
                log.warn("数据库中没有考勤组信息，跳过同步");
                return;
            }

            log.info("共发现 {} 个考勤组需要同步", groupList.size());

            // 2. 获取钉钉AccessToken
            String accessToken = getAccessToken();
            if (StrUtil.isBlank(accessToken)) {
                log.error("获取钉钉AccessToken失败，无法继续同步");
                return;
            }
            log.info("成功获取钉钉AccessToken");

            // 3. 计算查询时间范围（获取昨天的考勤信息）
            LocalDateTime now = LocalDateTime.now();
            // 凌晨2点执行，查询昨天（昨天0点到今天0点）
            LocalDateTime checkDateFrom = now.minusDays(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime checkDateTo = now.withHour(0).withMinute(0).withSecond(0);

            log.info("查询时间范围：{} 至 {}", 
                    checkDateFrom.format(DATE_TIME_FORMATTER),
                    checkDateTo.format(DATE_TIME_FORMATTER));

            // 4. 依次同步每个考勤组
            int totalSuccessCount = 0;
            int totalFailCount = 0;

            for (GroupKaoqin group : groupList) {
                String groupId = group.getGroupId();
                String groupName = group.getGroupName();

                try {
                    log.info("开始同步考勤组：{} (ID: {})", groupName, groupId);
                    // 调用Service层方法同步考勤记录
                    int count = dingtalkAttendanceRecordService.syncAttendanceRecordsByGroupId(
                            groupId, checkDateFrom, checkDateTo, accessToken);
                    totalSuccessCount += count;
                    log.info("考勤组 {} 同步完成，新增记录 {} 条", groupName, count);
                } catch (Exception e) {
                    totalFailCount++;
                    log.error("同步考勤组 {} 失败: {}", groupName, e.getMessage(), e);
                }
            }

            log.info("========== 定时考勤记录同步任务完成 ==========");
            log.info("成功同步 {} 个考勤组，失败 {} 个，共新增 {} 条记录",
                    groupList.size() - totalFailCount, totalFailCount, totalSuccessCount);

        } catch (Exception e) {
            log.error("定时考勤记录同步任务执行失败", e);
        }
    }

    /**
     * 获取钉钉AccessToken
     */
    private String getAccessToken() {
        try {
            Map<String, Object> result = dingUserService.getDingTalkUserInfo();
            if (result != null && Boolean.TRUE.equals(result.get("success"))) {
                return (String) result.get("accessToken");
            }
            log.error("获取AccessToken失败: {}", result.get("msg"));
            return null;
        } catch (Exception e) {
            log.error("获取AccessToken异常", e);
            return null;
        }
    }
}
