// @ts-ignore
/* eslint-disable */
import request from "@/request.ts";

/** 管理员分页查询系统用户 POST /sysUser/admin/page */
export async function sysUserListpage(
  body: API.SysUserQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageSysUserVO>("/sysUser/admin/page", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 更新用户权限更具系统用户id PUT /sysUser/admin/update/role */
export async function update(
  body: API.SysUserUpdateQueryReqyest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>("/sysUser/admin/update/role", {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}
