package com.cimhub.common.testsupport;

import com.cimhub.common.dto.response.ApiError;
import com.cimhub.common.dto.response.ApiSuccess;

public final class ApiResponses {

    private ApiResponses() {}

    public static <T> ApiSuccess<T> ok(T data) {
        return ApiSuccess.success(data);
    }

    public static <T> ApiSuccess<T> ok(T data, String message) {
        return ApiSuccess.success(data, message);
    }

    public static ApiError error(String code, String message) {
        return ApiError.builder().code(code).message(message).build();
    }

    public static ApiError error(String code, String message, String path) {
        return ApiError.of(0, code, message, path);
    }
}
