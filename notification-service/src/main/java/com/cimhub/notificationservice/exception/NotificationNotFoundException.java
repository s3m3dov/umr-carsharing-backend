package com.cimhub.notificationservice.exception;

import com.cimhub.common.exception.BusinessException;

public class NotificationNotFoundException extends BusinessException {

    public NotificationNotFoundException(String message) {
        super(NotificationErrorCode.NOTIFICATION_NOT_FOUND, message);
    }
}
