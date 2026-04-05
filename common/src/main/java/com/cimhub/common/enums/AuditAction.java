package com.cimhub.common.enums;

/**
 * Enumeration of all possible audit actions in the system.
 * Used for tracking operations performed by admins, users, and system processes.
 */
public enum AuditAction {
    CREATE,
    UPDATE,
    DELETE,
    APPROVE,
    REJECT,
    FLAG,
    CANCEL,
    PUBLISH,
    RESTORE,
    LOGIN,
    LOGOUT,
    BOOK,
    COMPLETE,
    REVIEW
}
