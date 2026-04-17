// @ts-ignore
/* eslint-disable */
import request from "@/request.ts";

/** 此处后端没有提供注释 POST /groupKaoqin/get/list/groups */
export async function getGroupList(
  body: API.GroupKaoqinQuertRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageGroupKaoqinVO>(
    "/groupKaoqin/get/list/groups",
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

/** 更新最新的考勤组情况 GET /groupKaoqin/get/simplegroup */
export async function getSimpleGroup(options?: { [key: string]: any }) {
  return request<API.BaseResponseInteger>("/groupKaoqin/get/simplegroup", {
    method: "GET",
    ...(options || {}),
  });
}
