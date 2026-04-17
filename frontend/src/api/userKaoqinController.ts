// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 此处后端没有提供注释 POST /userKaoqin/get/list/userkaoqins */
export async function getGroupList(
  body: API.UserKaoqinByGroupIdQuertRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageUserKaoqinVO>(
    "/userKaoqin/get/list/userkaoqins",
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      data: body,
      ...(options || {}),
    }
  );
}

/** 获取考勤人员信息并存入数据库中 通过考勤组id获取考勤人员信息并存入数据库 POST /userKaoqin/get/userId */
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
