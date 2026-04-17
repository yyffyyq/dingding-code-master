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
}
