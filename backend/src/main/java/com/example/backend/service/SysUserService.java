package com.example.backend.service;

import com.example.backend.model.dto.SysUserQueryRequest;
import com.example.backend.model.dto.SysUserUpdateQueryReqyest;
import com.example.backend.model.entity.SysUser;
import com.example.backend.model.vo.SysUserVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 *  服务层。
 *
 * @author <a href="https://github.com/yyffyyq">代码制造者yfy</a>
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 将登录的用户信息放入数据库然后做一个标记
     * @param sysUser 登录用户信息
     * @param request seesion需要修改的请求标志
     * @return 返回登录成功信息
     */
    SysUserVO userLogin(SysUser sysUser, HttpServletRequest request);

    /**
     * 封装系统用户信息
     * @param sysUser 系统用户对象
     * @return 封装后的系统用户信息
     */
    SysUserVO getSysUserVO(SysUser sysUser);

    /**
     * 获取用户登录状态以及用户信息
     * @param request http请求
     * @return 返回查询到的用户信息
     */
    SysUser getLoginUser(HttpServletRequest request);

    /**
     * 用户登出请求
     * @param request http请求
     * @return 是否退出成功
     */
    String logoutUser(HttpServletRequest request);

    /**
     * 用户分页sql查询语句生成
     * @param sysUserQueryRequest 分页查询请求
     * @return 组合好的sql查询语句
     */
    QueryWrapper getQueryWrapper(SysUserQueryRequest sysUserQueryRequest);

    /**
     * 封装系统用户列表
     * @param records 需要封装的列表
     * @return 封装后系统用户信息
     */
    List<SysUserVO> getUserVoList(List<SysUser> records);


    Boolean updateRoleById(SysUserUpdateQueryReqyest sysUserUpdateQueryReqyest);
}
