package com.example.backend.event;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.dingtalk.open.app.api.GenericEventListener;
import com.dingtalk.open.app.api.message.GenericOpenDingTalkEvent;
import com.dingtalk.open.app.stream.protocol.event.EventAckStatus;
import com.example.backend.model.dto.UserKaoqinDTO;
import com.example.backend.service.DingUserService;
import com.example.backend.service.UserKaoqinService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 钉钉事件订阅类，用于获取钉钉考勤组变更事件
 *
 * @author <a href="https://github.com/yyffyyq">代码制造者yfy</a>
 */
@Slf4j
public class EventConsumer implements GenericEventListener {

    /**
     * 时间窗口：5000毫秒 = 5秒
     */
    private static final long LOCK_TIME = 5000;

    /**
     * 钉钉Token默认有效期：2小时 = 7200秒，提前1分钟过期，避免临界值问题
     */
    private static final long TOKEN_EXPIRE_SECONDS = 7140;

    /**
     * 用于事件去重（存储已处理的事件ID）
     */
    private static final ConcurrentHashMap<Long, Long> GROUP_LAST_PROCESS_TIME = new ConcurrentHashMap<>();

    /**
     * 静态缓存：全局共享，线程安全
     */
    private static String CACHED_ACCESS_TOKEN;

    /**
     * 过期时间戳（毫秒）
     */
    private static long EXPIRE_TIME;

    private static final Object LOCK = new Object();

    private final DingUserService dingUserService;

    private final UserKaoqinService userKaoqinService;

    /**
     * 构造函数，通过EventListener传入依赖
     *
     * @param dingUserService 钉钉用户服务
     * @param userKaoqinService 考勤用户服务
     */
    public EventConsumer(DingUserService dingUserService, UserKaoqinService userKaoqinService) {
        this.dingUserService = dingUserService;
        this.userKaoqinService = userKaoqinService;
    }


    /**
     * 处理钉钉事件
     *
     * @param event 钉钉事件
     * @return 事件确认状态
     */
    @Override
    public EventAckStatus onEvent(GenericOpenDingTalkEvent event) {
        String eventType = event.getEventType();

        // 1. 只处理考勤组事件
        if (!"attend_group_change".equals(eventType)) {
            return EventAckStatus.SUCCESS;
        }

        // 2. 解析考勤组ID（核心去重依据）
        Long groupId = parseGroupId(event);
        if (groupId == null) {
            log.warn("解析考勤组ID失败，跳过处理");
            return EventAckStatus.SUCCESS;
        }

        // 3. 5秒内同一个考勤组只执行一次（防重处理）
        if (isDuplicateEvent(groupId)) {
            log.debug("考勤组 {} 在5秒内已处理过，跳过", groupId);
            return EventAckStatus.SUCCESS;
        }

        // 4. 更新最后处理时间
        updateLastProcessTime(groupId);

        log.info("处理考勤组变更事件, eventType={}, groupId={}, eventId={}",
                eventType, groupId, event.getEventId());

        // 5. 处理考勤组变更业务逻辑
        handleAttendGroupChange(groupId);

        return EventAckStatus.SUCCESS;
    }

    /**
     * 解析考勤组ID
     *
     * @param event 钉钉事件
     * @return 考勤组ID，解析失败返回null
     */
    private Long parseGroupId(GenericOpenDingTalkEvent event) {
        try {
            String dataStr = String.valueOf(event.getData());
            JSONObject jsonObject = JSONObject.parseObject(dataStr);
            String idStr = jsonObject.getString("id");
            return StrUtil.isNotBlank(idStr) ? Long.parseLong(idStr) : null;
        } catch (Exception e) {
            log.error("解析考勤组ID失败: {}", event.getData(), e);
            return null;
        }
    }

    /**
     * 检查是否为重复事件（5秒内同一个考勤组只执行一次）
     *
     * @param groupId 考勤组ID
     * @return true-重复事件，false-非重复事件
     */
    private boolean isDuplicateEvent(Long groupId) {
        long now = System.currentTimeMillis();
        Long lastTime = GROUP_LAST_PROCESS_TIME.get(groupId);
        return lastTime != null && (now - lastTime) < LOCK_TIME;
    }

    /**
     * 更新最后处理时间
     *
     * @param groupId 考勤组ID
     */
    private void updateLastProcessTime(Long groupId) {
        GROUP_LAST_PROCESS_TIME.put(groupId, System.currentTimeMillis());
    }

    /**
     * 处理考勤组变更事件
     *
     * @param groupId 考勤组ID
     */
    private void handleAttendGroupChange(Long groupId) {
        try {
            // 1. 获取AccessToken
            String accessToken = getAccessToken();
            if (StrUtil.isBlank(accessToken)) {
                log.error("获取钉钉AccessToken失败，无法处理考勤组变更事件");
                return;
            }
            log.info("考勤组 {} 变更，开始同步考勤组人员信息", groupId);

            // 2. 操作员ID（系统默认操作员）
            String operatorUserId = "381263334321485542";

            // 3. 获取更新后的考勤组内的员工信息
            List<UserKaoqinDTO> userDTOList = userKaoqinService.getMemeberListId(
                    String.valueOf(groupId), accessToken, operatorUserId);

            if (CollUtil.isEmpty(userDTOList)) {
                log.warn("考勤组 {} 内没有员工信息", groupId);
                return;
            }

            // 4. 批量插入考勤组人员
            List<String> idList = userKaoqinService.insertGroupList(userDTOList, String.valueOf(groupId));

            if (CollUtil.isEmpty(idList)) {
                log.warn("考勤组 {} 没有新增员工", groupId);
                return;
            }

            // 5. 获取并更新用户姓名
            String result = userKaoqinService.insertUserName(idList, accessToken);
            log.info("考勤组 {} 同步完成，结果: {}", groupId, result);

        } catch (Exception e) {
            log.error("处理考勤组 {} 变更事件失败", groupId, e);
        }
    }

    /**
     * 获取钉钉AccessToken（带缓存机制）
     * 优先从缓存获取，缓存过期则调用API获取新Token
     *
     * @return AccessToken
     */
    private String getAccessToken() {
        // 1. 缓存有效，直接返回
        if (CACHED_ACCESS_TOKEN != null && System.currentTimeMillis() < EXPIRE_TIME) {
            log.debug("从缓存获取钉钉AccessToken");
            return CACHED_ACCESS_TOKEN;
        }

        // 2. 缓存失效/无缓存，加锁防止并发重复请求
        synchronized (LOCK) {
            // 双重检查，防止多线程重复获取
            if (CACHED_ACCESS_TOKEN != null && System.currentTimeMillis() < EXPIRE_TIME) {
                return CACHED_ACCESS_TOKEN;
            }

            try {
                // 3. 调用Service接口获取新Token
                log.info("从钉钉API获取新的AccessToken...");
                Map<String, Object> resultMap = dingUserService.getDingTalkUserInfo();

                if (resultMap == null || !Boolean.TRUE.equals(resultMap.get("success"))) {
                    String errorMsg = resultMap != null ? (String) resultMap.get("msg") : "未知错误";
                    log.error("获取钉钉AccessToken失败: {}", errorMsg);
                    return null;
                }

                String newToken = (String) resultMap.get("accessToken");
                if (StrUtil.isBlank(newToken)) {
                    log.error("钉钉返回的AccessToken为空");
                    return null;
                }

                // 4. 更新缓存和过期时间
                CACHED_ACCESS_TOKEN = newToken;
                EXPIRE_TIME = System.currentTimeMillis() + TOKEN_EXPIRE_SECONDS * 1000;

                log.info("钉钉AccessToken缓存刷新成功，有效期{}秒", TOKEN_EXPIRE_SECONDS);
                return newToken;

            } catch (Exception e) {
                log.error("获取钉钉AccessToken异常", e);
                return null;
            }
        }
    }
}
