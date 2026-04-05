package com.cimhub.notificationservice.exception;

import com.cimhub.common.exception.BusinessException;

public class InvalidNotificationSenderException extends BusinessException {

    public InvalidNotificationSenderException(String message) {
        super(NotificationErrorCode.INVALID_NOTIFICATION_SENDER, message);
    }
}
