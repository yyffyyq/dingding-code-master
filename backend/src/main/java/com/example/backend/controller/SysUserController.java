package com.example.backend.controller;

import com.example.backend.annotion.AuthCheck;
import com.example.backend.common.BaseResponse;
import com.example.backend.common.ResultUtils;
import com.example.backend.constant.UserConstant;
import com.example.backend.exception.ErrorCode;
import com.example.backend.exception.ThrowUtils;
import com.example.backend.model.dto.sysUser.SysUserQueryRequest;
import com.example.backend.model.dto.sysUser.SysUserUpdateQueryReqyest;
import com.example.backend.model.entity.SysUser;
import com.example.backend.model.vo.sysUservo.SysUserVO;
import com.example.backend.service.SysUserService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 *  系统用户功能部分-控制层
 *
 * @author <a href="https://github.com/yyffyyq">代码制造者yfy</a>
 */
@RestController
@RequestMapping("/sysUser")
//@Tag(name = "系统用户功能部分")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 系统用户分页查询。
     *
     * @param sysUserQueryRequest 系统用户分页查询请求
     * @return 系统用户分页查询结果
     */
    @PostMapping("/admin/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "管理员分页查询系统用户")
    public BaseResponse<Page<SysUserVO>> sysUserListpage(@RequestBody SysUserQueryRequest sysUserQueryRequest){

        // 1. 判断分页查询请求是否为空
        ThrowUtils.throwIf(sysUserQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 2. 获取分页查询的页码和每页数量
        long pageNum = sysUserQueryRequest.getPageNum();
        long pageSize = sysUserQueryRequest.getPageSize();
        // 3. 创建分页查询对象，并传入查询系统用户的sql语句
        Page<SysUser> sysuserPage = sysUserService.page(Page.of(pageNum, pageSize),
                sysUserService.getQueryWrapper(sysUserQueryRequest));

        // 4. 数据封装脱敏
        Page<SysUserVO> sysuserVOPage = new Page<>(pageNum, pageSize, sysuserPage.getTotalRow());
        List<SysUserVO> sysuserVOList = sysUserService.getUserVoList(sysuserPage.getRecords());

        // 5. 把系统用户封装后信息放入封装分页中
        sysuserVOPage.setRecords(sysuserVOList);
        // 6. 返回前端封装后的系统用户分页信息
        return ResultUtils.success(sysuserVOPage);
    }


    /**
     * 根据系统用户id更新权限
     *
     * @param sysUserUpdateQueryReqyest 系统用户权限更新请求
     * @return 返回是否更新成功信息
     */
    @PutMapping("/admin/update/role")
    @Operation(summary = "更新系统用户权限")
    public BaseResponse<Boolean> update(@RequestBody SysUserUpdateQueryReqyest sysUserUpdateQueryReqyest) {

        // 1. 判断系统用户更新请求是否为空
        ThrowUtils.throwIf(sysUserUpdateQueryReqyest == null, ErrorCode.PARAMS_ERROR);

        // 2. 调用方法更新系统用户权限
        return ResultUtils.success(sysUserService.updateRoleById(sysUserUpdateQueryReqyest));
    }

}
