// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 获取钉钉token 获取钉钉token用于后续获取其他信息 GET /api/dingtalk/token */
export async function getDingTalkAccessToken(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/api/dingtalk/token", {
    method: "GET",
    ...(options || {}),
  });
}
