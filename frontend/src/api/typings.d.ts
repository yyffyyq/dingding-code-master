declare namespace API {
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

  type SysUserVO = {
    id?: number;
    nickName?: string;
    userRole?: string;
    avarUrl?: string;
  };
}
