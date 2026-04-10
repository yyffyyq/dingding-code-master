package com.example.backend.model.dto.sysUser;

import lombok.Data;

import java.io.Serializable;

/**
 * 系统用户更新请求
 */
@Data
public class SysUserUpdateQueryReqyest implements Serializable {

    /**
     * 系统用户id
     */
    private Long id;

    /**
     * 系统用户权限
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}
