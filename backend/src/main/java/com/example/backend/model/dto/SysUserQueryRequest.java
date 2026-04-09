package com.example.backend.model.dto;

import com.example.backend.common.PageRequest;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统用户查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysUserQueryRequest extends PageRequest implements Serializable {


    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户角色：admin-管理员, user-普通用户
     */
    private String userRole;

    /**
     * 用户头像
     */
    private String avarUrl;

    private static final long serialVersionUID = 1L;
}
