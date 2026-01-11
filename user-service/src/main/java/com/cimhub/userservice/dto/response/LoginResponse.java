package com.cimhub.userservice.dto.response;

import com.cimhub.common.enums.UserType;

public record LoginResponse(
        String username,
        String userId,
        UserType userType,
        boolean loginSuccess,
        String errMsg) {
}
