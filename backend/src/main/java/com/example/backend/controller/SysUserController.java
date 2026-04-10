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
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 *  控制层。
 *
 * @author <a href="https://github.com/yyffyyq">代码制造者yfy</a>
 */
@RestController
@RequestMapping("/sysUser")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 系统用户分页查询。
     *
     * @param sysUserQueryRequest 分页对象
     * @return 分页对象
     */
    @PostMapping("/admin/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "管理员分页查询系统用户")
    public BaseResponse<Page<SysUserVO>> sysUserListpage(@RequestBody SysUserQueryRequest sysUserQueryRequest) {
        ThrowUtils.throwIf(sysUserQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = sysUserQueryRequest.getPageNum();
        long pageSize = sysUserQueryRequest.getPageSize();

        Page<SysUser> sysuserPage = sysUserService.page(Page.of(pageNum, pageSize),
                sysUserService.getQueryWrapper(sysUserQueryRequest));

        // 数据脱敏
        Page<SysUserVO> sysuserVOPage = new Page<>(pageNum, pageSize, sysuserPage.getTotalRow());
        List<SysUserVO> sysuserVOList = sysUserService.getUserVoList(sysuserPage.getRecords());

        sysuserVOPage.setRecords(sysuserVOList);

        return ResultUtils.success(sysuserVOPage);
    }


    /**
     * 根据系统用户id更新权限
     *
     * @param sysUserUpdateQueryReqyest
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("/admin/update/role")
    @Operation(summary = "更新用户权限更具系统用户id")
    public BaseResponse<Boolean> update(@RequestBody SysUserUpdateQueryReqyest sysUserUpdateQueryReqyest) {

        ThrowUtils.throwIf(sysUserUpdateQueryReqyest == null, ErrorCode.PARAMS_ERROR);

        return ResultUtils.success(sysUserService.updateRoleById(sysUserUpdateQueryReqyest));
    }

}
