package com.example.backend.mapper;

import com.example.backend.model.entity.SysUser;
import com.mybatisflex.core.BaseMapper;

/**
 *  映射层。
 *
 * @author <a href="https://github.com/yyffyyq">代码制造者yfy</a>
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

    SysUser selectOneBySysUser(SysUser sysUser1);

    /**
     * 将信息存入数据库中
     * @param sysUser
     * @return
     */
    Long insertSysuer(SysUser sysUser);
}