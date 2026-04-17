package com.example.backend.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserKaoqinVO implements Serializable {
    /**
     * 用户名字
     */
    private String userName;

    /**
     * 创建时间
     */
    private String userKaoqinVOPage;
}
