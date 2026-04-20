declare namespace API {
  type BaseResponseBoolean = {
    code?: number;
    data?: boolean;
    message?: string;
  };

  type BaseResponseInteger = {
    code?: number;
    data?: number;
    message?: string;
  };

  type BaseResponseListDingtalkAttendanceRecordVO = {
    code?: number;
    data?: DingtalkAttendanceRecordVO[];
    message?: string;
  };

  type BaseResponsePageDingtalkAttendanceRecordVO = {
    code?: number;
    data?: PageDingtalkAttendanceRecordVO;
    message?: string;
  };

  type BaseResponsePageGroupKaoqinVO = {
    code?: number;
    data?: PageGroupKaoqinVO;
    message?: string;
  };

  type BaseResponsePageSysUserVO = {
    code?: number;
    data?: PageSysUserVO;
    message?: string;
  };

  type BaseResponsePageUserKaoqinVO = {
    code?: number;
    data?: PageUserKaoqinVO;
    message?: string;
  };

  type BaseResponseString = {
    code?: number;
    data?: string;
    message?: string;
  };

  type BaseResponseSysUserVO = {
    code?: number;
    data?: SysUserVO;
    message?: string;
  };

  type DingtalkAttendanceRecordQueryRequest = {
    pageNum?: number;
    pageSize?: number;
    sortField?: string;
    sortOrder?: string;
    userId?: string;
    groupId?: string;
    queryDate?: string;
    userName?: string;
    checkType?: string;
    timeResult?: string;
    locationResult?: string;
  };

  type DingtalkAttendanceRecordUpdateRequest = {
    groupId?: string;
    checkDateFrom?: string;
    checkDateTo?: string;
  };

  type DingtalkAttendanceRecordVO = {
    id?: number;
    recordId?: string;
    userId?: string;
    userName?: string;
    groupId?: string;
    workDate?: string;
    checkType?: string;
    userCheckTime?: string;
    planCheckTime?: string;
    timeResult?: string;
    locationResult?: string;
    isLegal?: string;
    isNormal?: boolean;
    sourceType?: string;
    locationMethod?: string;
    createTime?: string;
    updateTime?: string;
  };

  type getAttendanceRecordsByUserIdParams = {
    userId: string;
    queryDate?: string;
  };

  type getMyAttendanceRecordsParams = {
    queryDate?: string;
  };

  type getUserkaoqinParams = {
    group_id: string;
  };

  type GroupKaoqinQuertRequest = {
    pageNum?: number;
    pageSize?: number;
    sortField?: string;
    sortOrder?: string;
    groupId?: string;
    groupName?: string;
  };

  type GroupKaoqinVO = {
    groupId?: string;
    groupName?: string;
    isDeleted?: boolean;
  };

  type PageDingtalkAttendanceRecordVO = {
    records?: DingtalkAttendanceRecordVO[];
    pageNumber?: number;
    pageSize?: number;
    totalPage?: number;
    totalRow?: number;
    optimizeCountQuery?: boolean;
  };

  type PageGroupKaoqinVO = {
    records?: GroupKaoqinVO[];
    pageNumber?: number;
    pageSize?: number;
    totalPage?: number;
    totalRow?: number;
    optimizeCountQuery?: boolean;
  };

  type PageSysUserVO = {
    records?: SysUserVO[];
    pageNumber?: number;
    pageSize?: number;
    totalPage?: number;
    totalRow?: number;
    optimizeCountQuery?: boolean;
  };

  type PageUserKaoqinVO = {
    records?: UserKaoqinVO[];
    pageNumber?: number;
    pageSize?: number;
    totalPage?: number;
    totalRow?: number;
    optimizeCountQuery?: boolean;
  };

  type SysUserQueryRequest = {
    pageNum?: number;
    pageSize?: number;
    sortField?: string;
    sortOrder?: string;
    id?: number;
    nickName?: string;
    userRole?: string;
    avarUrl?: string;
  };

  type SysUserUpdateQueryReqyest = {
    id?: number;
    userRole?: string;
  };

  type SysUserVO = {
    id?: number;
    nickName?: string;
    userRole?: string;
    avarUrl?: string;
  };

  type UserKaoqinByGroupIdQuertRequest = {
    pageNum?: number;
    pageSize?: number;
    sortField?: string;
    sortOrder?: string;
    userName?: string;
    groupId?: string;
  };

  type UserKaoqinVO = {
    userId?: string;
    userName?: string;
    createTime?: string;
  };
}
