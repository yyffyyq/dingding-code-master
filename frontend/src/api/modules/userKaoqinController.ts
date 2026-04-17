// @ts-ignore
/* eslint-disable */
import request from "@/request.ts";

/** 此处后端没有提供注释 POST /userKaoqin/get/userId */
export async function getUserkaoqin(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getUserkaoqinParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseString>("/userKaoqin/get/userId", {
    method: "POST",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}
