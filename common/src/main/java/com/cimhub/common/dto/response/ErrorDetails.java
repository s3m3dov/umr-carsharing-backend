package com.cimhub.common.dto.response;

import java.util.List;

public final class ErrorDetails {

    private ErrorDetails() {
    }

    public static List<ErrorDetail> empty() {
        return List.of();
    }

    public static List<ErrorDetail> single(String message) {
        return List.of(ErrorDetail.of(message));
    }

    public static List<ErrorDetail> of(List<ErrorDetail> details) {
        return details != null ? List.copyOf(details) : List.of();
    }
}
