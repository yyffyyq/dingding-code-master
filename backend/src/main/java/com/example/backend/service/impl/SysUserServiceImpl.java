package com.example.backend.service.impl;

import com.example.backend.exception.BusinessException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.exception.ThrowUtils;
import com.example.backend.mapper.SysUserMapper;
import com.example.backend.model.entity.SysUser;
import com.example.backend.model.vo.SysUserVO;
import com.example.backend.service.SysUserService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.example.backend.constant.UserConstant.USER_LOGIN_STATE;

/**
 *  服务层实现。
 *
 * @author <a href="https://github.com/yyffyyq">代码制造者yfy</a>
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>  implements SysUserService {

    @Resource
    private SysUserMapper sysUserMapper;


    /**
     *
     * @param sysUser 登录用户信息
     * @param request
     * @return 返回封装系统用户信息
     * @exception BusinessException 请求传入系统用户为空抛出
     */
    @Override
    public SysUserVO userLogin(SysUser sysUser, HttpServletRequest request) {

        // 先判断传入值是否为空
        ThrowUtils.throwIf(sysUser==null, ErrorCode.SYS_USER_LOGIN_PARAMS_ERROR);

        // 判断用户是否在数据库中，通过union_id来判断1个
        SysUser sysUser1 = new SysUser();
        sysUser1.setUnionId(sysUser.getUnionId());
        SysUser result = sysUserMapper.selectOneBySysUser(sysUser1);
        if (result != null){
            sysUser.setId(result.getId());
        }
        // 没有在数据库中就存进去
        if (result == null){

            // 将用户信息存入数据库中
            try{

                // 添加并获取添加id，为了后续存入 session 做准备
                sysUser.setCreateTime(LocalDateTime.now());
                Long insertSysuserId = sysUserMapper.insertSysuer(sysUser);
                sysUser.setId(insertSysuserId);

            }catch (Exception e){

                System.out.println("报错信息为："+e);
            }
        }

        // 更新 session 内用户状态为了之后的获取登录情况
        request.getSession().setAttribute(USER_LOGIN_STATE,sysUser);

        // 返回封装数据给前端展示
        return getSysUserVO(sysUser);
    }

    /**
     * 封装系统用户数据
     * 所有对于SysUser对象的封装都可以使用这个函数
     * @param sysUser 系统用户对象
     * @return 返回封装后用户信息
     * @exception BusinessException 请求封装对象为空抛出
     */
    @Override
    public SysUserVO getSysUserVO(SysUser sysUser) {

        // 判断封装对象是否为空
        if(sysUser==null){
            throw new BusinessException(ErrorCode.SYS_USER_VO_LOGIN_PARAMS_ERROR);
        }

        // 创建封装对象，并赋值
        SysUserVO sysUserVO = new SysUserVO();
        BeanUtils.copyProperties(sysUser,sysUserVO);

        // 返回封装数据
        return sysUserVO;
    }

    /**
     * 查询当前登录用户信息
     * @param request http请求
     * @return 返回当前用户信息
     * @exception BusinessException http请求为空抛出/用户未登录抛出/用户不存在抛出
     */
    @Override
    public SysUser getLoginUser(HttpServletRequest request) {

        // 判断请求是否为空
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR,"http请求参数为空");

        // 从会话中获取用户信息
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        SysUser currentSysUser = (SysUser) userObj;
        if (!(userObj instanceof SysUser)){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (currentSysUser == null || currentSysUser.getId() == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 获取用户编号并查询数据库，用于判断是否存在用户
        Long userId = currentSysUser.getId();
        currentSysUser = this.getById(userId);
        if (currentSysUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 返回这个用户信息
        return currentSysUser;
    }

    /**
     * 退出登录
     * @param request http请求
     * @return
     */
    @Override
    public String logoutUser(HttpServletRequest request) {

        // 判断请求是否为空
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);

        // 判断是否有登录用户
        SysUser sysUser = getLoginUser(request);
        ThrowUtils.throwIf(sysUser == null, ErrorCode.NOT_LOGIN_ERROR,"在退出用户操作中当前未登录");

        // 清空 session user_login 内数据
        request.getSession().removeAttribute(USER_LOGIN_STATE);

        // 这里再做一个判断为了之后返回值
        Object loginSate = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginSate != null){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"退出登录状态失败");
        }

        // 返回是否清除成功请求
        return "退出成功";
    }
}
