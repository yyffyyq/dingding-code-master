package com.example.backend.model.dto;

import com.example.backend.common.PageRequest;

import java.io.Serializable;

public class UserKaoqinByGroupIdQuertRequest extends PageRequest implements Serializable {

    /**
     * 用户名字
     */
    private String userName;

    /**
     * 考勤主id
     */
    private String groupId;

    private static final long serialVersionUID = 1L;
}
