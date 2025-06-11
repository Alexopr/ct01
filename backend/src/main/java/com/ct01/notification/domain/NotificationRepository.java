package com.ct01.notification.domain;

import com.ct01.core.domain.Repository;
import com.ct01.core.domain.UserId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository для работы с уведомлениями
 */
public interface NotificationRepository extends Repository<Notification, NotificationId> {
    
    /**
     * Найти уведомление по ID
     */
    Optional<Notification> findById(NotificationId id);
    
    /**
     * Сохранить уведомление
     */
    Notification save(Notification notification);
    
    /**
     * Удалить уведомление
     */
    void delete(NotificationId id);
    
    /**
     * Найти уведомления пользователя
     */
    List<Notification> findByRecipientId(UserId recipientId);
    
    /**
     * Найти уведомления по статусу
     */
    List<Notification> findByStatus(NotificationStatus status);
    
    /**
     * Найти уведомления по каналу
     */
    List<Notification> findByChannel(NotificationChannelType channelType);
    
    /**
     * Найти уведомления по категории
     */
    List<Notification> findByCategory(NotificationCategory category);
    
    /**
     * Найти уведомления по приоритету
     */
    List<Notification> findByPriority(NotificationPriority priority);
    
    /**
     * Найти уведомления пользователя по статусу
     */
    List<Notification> findByRecipientIdAndStatus(UserId recipientId, NotificationStatus status);
    
    /**
     * Найти непрочитанные уведомления пользователя
     */
    List<Notification> findUnreadByRecipientId(UserId recipientId);
    
    /**
     * Найти уведомления для повтора отправки
     */
    List<Notification> findForRetry(int maxRetries);
    
    /**
     * Найти истекшие уведомления
     */
    List<Notification> findExpired();
    
    /**
     * Найти уведомления для отправки
     */
    List<Notification> findPendingForDelivery();
    
    /**
     * Найти уведомления, созданные в период времени
     */
    List<Notification> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * Найти уведомления пользователя с пагинацией
     */
    List<Notification> findByRecipientIdWithPagination(UserId recipientId, int page, int size);
    
    /**
     * Посчитать непрочитанные уведомления пользователя
     */
    long countUnreadByRecipientId(UserId recipientId);
    
    /**
     * Посчитать уведомления по статусу
     */
    long countByStatus(NotificationStatus status);
    
    /**
     * Найти уведомления с высоким приоритетом для немедленной отправки
     */
    List<Notification> findHighPriorityPending();
    
    /**
     * Удалить старые уведомления (старше указанной даты)
     */
    void deleteOlderThan(LocalDateTime threshold);
    
    /**
     * Найти уведомления по нескольким статусам
     */
    List<Notification> findByStatusIn(List<NotificationStatus> statuses);
    
    /**
     * Найти уведомления пользователя определенной категории
     */
    List<Notification> findByRecipientIdAndCategory(UserId recipientId, NotificationCategory category);
    
    /**
     * Пометить все уведомления пользователя как прочитанные
     */
    void markAllAsReadForUser(UserId recipientId);
    
    /**
     * Найти уведомления, которые должны истечь в ближайшее время
     */
    List<Notification> findExpiringWithin(long minutes);
}
