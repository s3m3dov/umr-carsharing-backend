package com.cimhub.notificationservice.exception;

import com.cimhub.common.exception.BusinessException;

public class UnauthorizedNotificationAccessException extends BusinessException {

    public UnauthorizedNotificationAccessException(String message) {
        super(NotificationErrorCode.UNAUTHORIZED_NOTIFICATION_ACCESS, message);
    }
}
