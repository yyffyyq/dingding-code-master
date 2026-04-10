package com.example.backend.model.dto.sysUser;

import com.example.backend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

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
