package com.example.backend.event;

import com.dingtalk.open.app.api.OpenDingTalkClient;
import com.dingtalk.open.app.api.OpenDingTalkStreamClientBuilder;
import com.dingtalk.open.app.api.security.AuthClientCredential;
import com.example.backend.service.DingUserService;
import com.example.backend.service.UserKaoqinService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 事件监听类，监听钉钉事件
 *
 * @author <a href="https://github.com/yyffyyq">代码制造者yfy</a>
 */
@Component
public class EventListener {

    @Value("${dingtalk.app.client-id}")
    private String clientId;

    @Value("${dingtalk.app.client-secret}")
    private String clientSecret;

    private final DingUserService dingUserService;

    private final UserKaoqinService userKaoqinService;

    @Autowired
    public EventListener(DingUserService dingUserService, UserKaoqinService userKaoqinService) {
        this.dingUserService = dingUserService;
        this.userKaoqinService = userKaoqinService;
    }

    @PostConstruct
    public void init() throws Exception {
        // 创建EventConsumer并注入依赖
        EventConsumer eventConsumer = new EventConsumer(dingUserService, userKaoqinService);

        // init stream client
        OpenDingTalkClient client = OpenDingTalkStreamClientBuilder
                .custom()
                .credential(new AuthClientCredential(clientId, clientSecret))
                .registerAllEventListener(eventConsumer)
                .build();
        client.start();
    }
}