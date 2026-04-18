package com.example.backend.event;

import com.alibaba.fastjson.JSONObject;
import com.dingtalk.open.app.api.GenericEventListener;
import com.dingtalk.open.app.api.message.GenericOpenDingTalkEvent;
import com.dingtalk.open.app.stream.protocol.event.EventAckStatus;
import com.example.backend.model.dto.UserKaoqinDTO;
import com.example.backend.service.DingUserService;
import com.example.backend.service.UserKaoqinService;
import com.example.backend.util.SpringContextUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component; // 新增导入

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 钉钉事件订阅类，用于获取钉钉考勤组变更事件
 *
 */
// 用于事件去重（存储已处理的事件ID）
public class EventConsumer implements GenericEventListener {


    private static final ConcurrentHashMap<Long, Long> GROUP_LAST_PROCESS_TIME = new ConcurrentHashMap<>();
    /**
     * 时间窗口：5000毫秒 = 5秒
     */
    private static final long LOCK_TIME = 5000;

    // token 缓存处理
    // 静态缓存：全局共享，线程安全
    private static String CACHED_ACCESS_TOKEN;
    // 过期时间戳（毫秒）
    private static long EXPIRE_TIME;
    // 钉钉Token默认有效期：2小时 = 7200秒，提前1分钟过期，避免临界值问题
    private static final long TOKEN_EXPIRE_SECONDS = 7140;

    private static final Object LOCK = new Object();


    @Override
    public EventAckStatus onEvent(GenericOpenDingTalkEvent event) {
        String eventType = event.getEventType();

        // 1. 只处理考勤组事件
        if (!"attend_group_change".equals(eventType)) {
            return EventAckStatus.SUCCESS;
        }

        // 2. 解析考勤组ID（核心去重依据）
        long groupId = 0;
        try {
            groupId = Long.parseLong(com.alibaba.fastjson.JSONObject.parseObject(String.valueOf(event.getData())).getString("id"));
        } catch (Exception e) {
            return EventAckStatus.SUCCESS;
        }

        // 5秒内同一个考勤组只执行一次
        long now = System.currentTimeMillis();
        Long lastTime = GROUP_LAST_PROCESS_TIME.get(groupId);
        // 如果5秒内处理过，直接跳过
        if (lastTime != null && now - lastTime < LOCK_TIME) {
            return EventAckStatus.SUCCESS;
        }
        // 更新最后处理时间
        GROUP_LAST_PROCESS_TIME.put(groupId, now);

        // 4. 最终只打印/处理一次
        System.out.println(String.format("处理考勤组事件,\n" +
                        "  考勤组ID=%s,\n" +
                        "  eventId=%s\n",
                eventType,
                groupId,
                event.getEventId()));

        // ===================== 这里写你的业务代码（同步数据库、增删人员） =====================
        switch (eventType){
            case "attend_group_change":

                String result = "无操作";

                // 获取token
                String AccessToken = getAccessToken();
                System.out.println("考勤表更新，开始调用更新考勤组操作,获取到token:"+AccessToken);
                String user_id = "381263334321485542";
                UserKaoqinService userService = SpringContextUtil.getBean(UserKaoqinService.class);

                // 获取更新后的考勤组内的员工信息
                List<UserKaoqinDTO> userDTOList = userService.getMemeberListId(String.valueOf(groupId),AccessToken,user_id);
                //获取后批量插入
                List<String> idList = new ArrayList<>();
                idList = userService.insertGroupList(userDTOList,String.valueOf(groupId));
                // 6. 通过用户user_idList(idList) 获取用户namelist
                result = userService.insertUserName(idList,AccessToken);
                System.out.println(result);
                break;
        }

        // 只会执行一次！
        return EventAckStatus.SUCCESS;
    }

    /**
     * 获取 access_token 请求
     * ，并将token放入缓存中
     * ，优先去缓存中获取token
     * ，如果token过期再去调用api获取token
     * @return
     */
    private String getAccessToken() {
        // 1. 缓存有效，直接返回
        if (CACHED_ACCESS_TOKEN != null && System.currentTimeMillis() < EXPIRE_TIME) {
            return CACHED_ACCESS_TOKEN;
        }

        // 2. 缓存失效/无缓存，加锁防止并发重复请求
        synchronized (LOCK) {
            // 双重检查，防止多线程重复获取
            if (CACHED_ACCESS_TOKEN != null && System.currentTimeMillis() < EXPIRE_TIME) {
                return CACHED_ACCESS_TOKEN;
            }

            try {
                // 3. 从Spring获取Service，调用接口获取新Token
                DingUserService dingUserService = SpringContextUtil.getBean(DingUserService.class);
                Map<String, Object> resultMap = dingUserService.getDingTalkUserInfo();
                String newToken = resultMap.get("accessToken").toString();
                System.out.println("token获取...");
                // 4. 更新缓存和过期时间
                CACHED_ACCESS_TOKEN = newToken;
                EXPIRE_TIME = System.currentTimeMillis() + TOKEN_EXPIRE_SECONDS * 1000;

                System.out.println("✅ Token缓存刷新成功，有效期2小时");
                return newToken;
            } catch (Exception e) {
                System.err.println("❌ 获取钉钉Token失败：" + e.getMessage());
                throw new RuntimeException("获取Token失败，事件处理中断");
            }
        }
    }
}
