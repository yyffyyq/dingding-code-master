package com.example.backend.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40101, "无权限"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败"),


    /**
     * 系统用户登录相关异常
     * 代码范围: 60001-60500
     */
    SYS_USER_LOGIN_PARAMS_ERROR(60001, "登录用户信息为空，请检查"),
    SYS_USER_VO_LOGIN_PARAMS_ERROR(60002, "封装对象为空"),
    SYS_USER_SCAN_LOGIN_PARAMS_ERROR(60003,"authCode不能为空"),
    SYS_USER_GET_USER_ACESS_TOKEN_FAIL_ERROR(60004,"获取钉钉userAccessToken失败"),
    SYS_USER_SCAN_LOGIN_ERROR(60005,"钉钉扫码登录异常"),
    SYS_USER_COMMONG_ERROR(60500, "系统用户通用报错"),


    /**
     * 系统用户登录相关异常
     * 代码范围: 60501-70000
     */
    GROUP_KAOQIN_GET_ERROR(70001,"获取考勤组失败"),
    GROUP_KAOQIN_GET_API_ERROR(70002,"调用钉钉API获取考勤组失败"),
    GROUP_KAOQIN_VO_LOGIN_PARAMS_ERROR(70003, "考勤组封装对象为空"),
    GROUP_KAOQIN_COMMONG_ERROR(70000, "考勤组通用报错");






    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}

