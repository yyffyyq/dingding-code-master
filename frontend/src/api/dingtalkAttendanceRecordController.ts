// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 分页查询考勤组考勤记录 查看当日考勤组内所有考勤人员的考勤信息，默认查询今天 POST /dingtalkAttendanceRecord/group/list */
export async function getAttendanceRecordsByGroupId(
  body: API.DingtalkAttendanceRecordQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageDingtalkAttendanceRecordVO>(
    "/dingtalkAttendanceRecord/group/list",
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

/** 获取我的考勤记录 当前用户查看自己的考勤情况，默认查询今天 GET /dingtalkAttendanceRecord/my/records */
export async function getMyAttendanceRecords(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getMyAttendanceRecordsParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseListDingtalkAttendanceRecordVO>(
    "/dingtalkAttendanceRecord/my/records",
    {
      method: "GET",
      params: {
        ...params,
      },
      ...(options || {}),
    }
  );
}

/** 同步考勤组考勤记录 根据考勤组ID从钉钉API获取并更新考勤记录，自动获取昨天的考勤信息（以2:00为第二天计算） POST /dingtalkAttendanceRecord/sync/group */
export async function syncAttendanceRecordsByGroupId(
  body: API.DingtalkAttendanceRecordUpdateRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseInteger>(
    "/dingtalkAttendanceRecord/sync/group",
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

/** 获取指定用户的考勤记录 管理员查看单个考勤人员的考勤情况，默认查询今天 GET /dingtalkAttendanceRecord/user/${param0} */
export async function getAttendanceRecordsByUserId(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getAttendanceRecordsByUserIdParams,
  options?: { [key: string]: any }
) {
  const { userId: param0, ...queryParams } = params;
  return request<API.BaseResponseListDingtalkAttendanceRecordVO>(
    `/dingtalkAttendanceRecord/user/${param0}`,
    {
      method: "GET",
      params: {
        ...queryParams,
      },
      ...(options || {}),
    }
  );
}
