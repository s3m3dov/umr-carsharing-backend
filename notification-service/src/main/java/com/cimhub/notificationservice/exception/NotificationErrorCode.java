package com.cimhub.notificationservice.exception;

import com.cimhub.common.exception.ErrorType;
import com.cimhub.common.exception.ServiceErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements ServiceErrorCode {

    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Notification not found", ErrorType.NOT_FOUND),
    UNAUTHORIZED_NOTIFICATION_ACCESS(HttpStatus.FORBIDDEN, "Unauthorized access to notification", ErrorType.AUTH_ERROR),
    INVALID_NOTIFICATION_SENDER(HttpStatus.BAD_REQUEST, "Invalid notification sender", ErrorType.VALIDATION_ERROR);

    private final HttpStatus httpStatus;
    private final String defaultMessage;
    private final ErrorType defaultType;

    @Override
    public String code() {
        return name();
    }
}
