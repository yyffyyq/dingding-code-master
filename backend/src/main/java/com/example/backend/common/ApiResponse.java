package com.example.backend.common;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String errorCode;
    private String requestId;

    // ===================== 成功方法（保留你原有的） =====================
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setData(data);
        response.setMessage("操作成功");
        return response;
    }

    // 成功：无数据
    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    // ===================== 失败方法（新增！解决你的报错） =====================
    // 1. 仅返回错误信息（最常用）
    public static <T> ApiResponse<T> fail(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setErrorCode("-1");
        return response;
    }

    // 2. 返回错误信息 + 错误码
    public static <T> ApiResponse<T> fail(String message, String errorCode) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setErrorCode(errorCode);
        return response;
    }

    // ===================== 原有 error 方法（保留） =====================
    public static <T> ApiResponse<T> error(String message, String errorCode, String requestId) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setErrorCode(errorCode);
        response.setRequestId(requestId);
        return response;
    }
}