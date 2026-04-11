package com.cimhub.common.testsupport;

import com.cimhub.common.dto.ApiResponseWrapper;

public final class ApiResponses {

    private ApiResponses() {
    }

    public static <T> ApiResponseWrapper<T> ok(T data) {
        return ApiResponseWrapper.success(data);
    }

    public static <T> ApiResponseWrapper<T> ok(T data, String message) {
        return ApiResponseWrapper.success(data, message);
    }

    public static <T> ApiResponseWrapper<T> error(String code, String message) {
        return ApiResponseWrapper.error(code, message);
    }

    public static <T> ApiResponseWrapper<T> error(String code, String message, String path) {
        return ApiResponseWrapper.error(code, message, path);
    }
}
