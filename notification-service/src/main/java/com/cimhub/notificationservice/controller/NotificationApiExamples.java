package com.cimhub.notificationservice.controller;

import com.cimhub.notificationservice.dto.examples.NotificationApiDoc;

/**
 * API examples for Notification endpoints.
 * @deprecated Use {@link NotificationApiDoc} instead.
 */
@Deprecated
public final class NotificationApiExamples {

    private NotificationApiExamples() {}

    public static final String CREATE_NOTIFICATION_RESPONSE = NotificationApiDoc.CREATE_NOTIFICATION_RESPONSE;
    public static final String NOTIFICATION_STATS_RESPONSE = NotificationApiDoc.NOTIFICATION_STATS_RESPONSE;
    public static final String ERROR_NOTIFICATION_NOT_FOUND = NotificationApiDoc.ERROR_NOTIFICATION_NOT_FOUND;
    public static final String ERROR_UNAUTHORIZED = NotificationApiDoc.ERROR_UNAUTHORIZED;
    public static final String ERROR_INVALID_SENDER = NotificationApiDoc.ERROR_INVALID_SENDER;
}