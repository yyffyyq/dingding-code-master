// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 获取用户登录状态 获取用户登录状态 GET /dingUser/get/login */
export async function health(options?: { [key: string]: any }) {
  return request<API.BaseResponseSysUserVO>("/dingUser/get/login", {
    method: "GET",
    ...(options || {}),
  });
}

/** 钉钉扫码/授权登录 获取钉钉用户信息及userId POST /dingUser/login */
export async function dingLogin(
  body: Record<string, any>,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseSysUserVO>("/dingUser/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 用户登录状态退出 用户退出登录，清空sseion POST /dingUser/logout */
export async function logout(options?: { [key: string]: any }) {
  return request<API.BaseResponseString>("/dingUser/logout", {
    method: "POST",
    ...(options || {}),
  });
}
