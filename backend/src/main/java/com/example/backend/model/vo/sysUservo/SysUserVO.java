package com.example.backend.model.vo.sysUservo;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class SysUserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Auto)
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
}
