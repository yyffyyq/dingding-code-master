declare namespace API {
  type BaseResponseBoolean = {
    code?: number;
    data?: boolean;
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
