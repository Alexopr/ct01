package com.ct01.notification.infrastructure.persistence.entity;

/**
 * JPA Enum для статуса уведомления
 */
public enum NotificationStatusJpa {
    PENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED,
    EXPIRED
} 
