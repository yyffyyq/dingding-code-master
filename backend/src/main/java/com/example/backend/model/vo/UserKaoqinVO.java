package com.example.backend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class UserKaoqinVO implements Serializable {
    /**
     * 用户id
     */
    private String userId;
    /**
     * 用户名字
     */
    private String userName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
