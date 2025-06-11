package com.ct01.notification.application;

import com.ct01.notification.domain.*;
import com.ct01.core.domain.UserId;

import java.util.List;

/**
 * Application Facade для Notification Domain
 * Упрощенный интерфейс для работы с уведомлениями
 */
public class NotificationApplicationFacade {
    
    private final NotificationUseCases.CreateNotificationUseCase createNotificationUseCase;
    private final NotificationUseCases.SendNotificationUseCase sendNotificationUseCase;
    private final NotificationUseCases.GetUserNotificationsUseCase getUserNotificationsUseCase;
    private final NotificationUseCases.MarkNotificationAsReadUseCase markAsReadUseCase;
    private final NotificationUseCases.MarkAllNotificationsAsReadUseCase markAllAsReadUseCase;
    private final NotificationUseCases.GetNotificationStatisticsUseCase getStatisticsUseCase;
    private final NotificationUseCases.HandleFailedNotificationUseCase handleFailedUseCase;
    private final NotificationUseCases.HandleDeliveredNotificationUseCase handleDeliveredUseCase;
    private final NotificationUseCases.GetNotificationsForDeliveryUseCase getForDeliveryUseCase;
    private final NotificationUseCases.GetNotificationsForRetryUseCase getForRetryUseCase;
    private final NotificationUseCases.CleanupExpiredNotificationsUseCase cleanupUseCase;
    
    public NotificationApplicationFacade(
            NotificationRepository notificationRepository,
            NotificationDomainService domainService) {
        
        this.createNotificationUseCase = new NotificationUseCases.CreateNotificationUseCase(notificationRepository, domainService);
        this.sendNotificationUseCase = new NotificationUseCases.SendNotificationUseCase(notificationRepository);
        this.getUserNotificationsUseCase = new NotificationUseCases.GetUserNotificationsUseCase(notificationRepository);
        this.markAsReadUseCase = new NotificationUseCases.MarkNotificationAsReadUseCase(notificationRepository);
        this.markAllAsReadUseCase = new NotificationUseCases.MarkAllNotificationsAsReadUseCase(notificationRepository);
        this.getStatisticsUseCase = new NotificationUseCases.GetNotificationStatisticsUseCase(domainService);
        this.handleFailedUseCase = new NotificationUseCases.HandleFailedNotificationUseCase(notificationRepository);
        this.handleDeliveredUseCase = new NotificationUseCases.HandleDeliveredNotificationUseCase(notificationRepository);
        this.getForDeliveryUseCase = new NotificationUseCases.GetNotificationsForDeliveryUseCase(domainService);
        this.getForRetryUseCase = new NotificationUseCases.GetNotificationsForRetryUseCase(domainService);
        this.cleanupUseCase = new NotificationUseCases.CleanupExpiredNotificationsUseCase(domainService);
    }
    
    // Основные операции
    
    /**
     * Создать уведомление
     */
    public NotificationId createNotification(String title, String message, NotificationCategory category, 
                                           NotificationChannelType channelType, String address, UserId recipientId) {
        
        NotificationContent content = new NotificationContent(title, message, category);
        NotificationChannel channel = new NotificationChannel(channelType, address);
        
        return createNotificationUseCase.execute(
            new NotificationUseCases.CreateNotificationUseCase.CreateNotificationCommand(content, channel, recipientId)
        );
    }
    
    /**
     * Создать уведомление с полными параметрами
     */
    public NotificationId createNotification(NotificationContent content, NotificationChannel channel, UserId recipientId) {
        return createNotificationUseCase.execute(
            new NotificationUseCases.CreateNotificationUseCase.CreateNotificationCommand(content, channel, recipientId)
        );
    }
    
    /**
     * Отправить уведомление
     */
    public void sendNotification(NotificationId notificationId) {
        sendNotificationUseCase.execute(
            new NotificationUseCases.SendNotificationUseCase.SendNotificationCommand(notificationId)
        );
    }
    
    /**
     * Получить все уведомления пользователя
     */
    public List<Notification> getUserNotifications(UserId userId) {
        return getUserNotificationsUseCase.execute(
            new NotificationUseCases.GetUserNotificationsUseCase.GetUserNotificationsQuery(userId)
        );
    }
    
    /**
     * Получить непрочитанные уведомления пользователя
     */
    public List<Notification> getUnreadNotifications(UserId userId) {
        return getUserNotificationsUseCase.execute(
            new NotificationUseCases.GetUserNotificationsUseCase.GetUserNotificationsQuery(
                userId, null, null, true, null, null
            )
        );
    }
    
    /**
     * Получить уведомления пользователя с пагинацией
     */
    public List<Notification> getUserNotifications(UserId userId, int page, int size) {
        return getUserNotificationsUseCase.execute(
            new NotificationUseCases.GetUserNotificationsUseCase.GetUserNotificationsQuery(
                userId, null, null, false, page, size
            )
        );
    }
    
    /**
     * Получить уведомления пользователя по статусу
     */
    public List<Notification> getUserNotificationsByStatus(UserId userId, NotificationStatus status) {
        return getUserNotificationsUseCase.execute(
            new NotificationUseCases.GetUserNotificationsUseCase.GetUserNotificationsQuery(
                userId, status, null, false, null, null
            )
        );
    }
    
    /**
     * Получить уведомления пользователя по категории
     */
    public List<Notification> getUserNotificationsByCategory(UserId userId, NotificationCategory category) {
        return getUserNotificationsUseCase.execute(
            new NotificationUseCases.GetUserNotificationsUseCase.GetUserNotificationsQuery(
                userId, null, category, false, null, null
            )
        );
    }
    
    /**
     * Пометить уведомление как прочитанное
     */
    public void markAsRead(NotificationId notificationId, UserId userId) {
        markAsReadUseCase.execute(
            new NotificationUseCases.MarkNotificationAsReadUseCase.MarkAsReadCommand(notificationId, userId)
        );
    }
    
    /**
     * Пометить все уведомления пользователя как прочитанные
     */
    public void markAllAsRead(UserId userId) {
        markAllAsReadUseCase.execute(
            new NotificationUseCases.MarkAllNotificationsAsReadUseCase.MarkAllAsReadCommand(userId)
        );
    }
    
    /**
     * Получить статистику уведомлений пользователя
     */
    public NotificationDomainService.NotificationStatistics getUserStatistics(UserId userId) {
        return getStatisticsUseCase.execute(
            new NotificationUseCases.GetNotificationStatisticsUseCase.GetStatisticsQuery(userId)
        );
    }
    
    // Административные операции
    
    /**
     * Обработать неудачную отправку уведомления
     */
    public void handleFailedNotification(NotificationId notificationId, String reason) {
        handleFailedUseCase.execute(
            new NotificationUseCases.HandleFailedNotificationUseCase.HandleFailedCommand(notificationId, reason)
        );
    }
    
    /**
     * Обработать успешную доставку уведомления
     */
    public void handleDeliveredNotification(NotificationId notificationId) {
        handleDeliveredUseCase.execute(
            new NotificationUseCases.HandleDeliveredNotificationUseCase.HandleDeliveredCommand(notificationId)
        );
    }
    
    /**
     * Получить уведомления готовые к отправке
     */
    public List<Notification> getNotificationsForDelivery() {
        return getForDeliveryUseCase.execute();
    }
    
    /**
     * Получить уведомления для повтора отправки
     */
    public List<Notification> getNotificationsForRetry() {
        return getForRetryUseCase.execute();
    }
    
    /**
     * Очистить устаревшие уведомления
     */
    public void cleanupExpiredNotifications() {
        cleanupUseCase.execute(new NotificationUseCases.CleanupExpiredNotificationsUseCase.CleanupCommand());
    }
    
    /**
     * Очистить устаревшие уведомления с указанием периода хранения
     */
    public void cleanupExpiredNotifications(int daysToKeep) {
        cleanupUseCase.execute(new NotificationUseCases.CleanupExpiredNotificationsUseCase.CleanupCommand(daysToKeep));
    }
    
    // Вспомогательные методы для быстрого создания уведомлений
    
    /**
     * Создать системное уведомление
     */
    public NotificationId createSystemNotification(String title, String message, UserId recipientId, NotificationChannelType channelType, String address) {
        return createNotification(title, message, NotificationCategory.SYSTEM, channelType, address, recipientId);
    }
    
    /**
     * Создать уведомление безопасности
     */
    public NotificationId createSecurityNotification(String title, String message, UserId recipientId, NotificationChannelType channelType, String address) {
        return createNotification(title, message, NotificationCategory.SECURITY, channelType, address, recipientId);
    }
    
    /**
     * Создать ценовой алерт
     */
    public NotificationId createPriceAlert(String title, String message, UserId recipientId, NotificationChannelType channelType, String address) {
        return createNotification(title, message, NotificationCategory.PRICE_ALERT, channelType, address, recipientId);
    }
    
    /**
     * Создать критичный алерт
     */
    public NotificationId createCriticalAlert(String title, String message, UserId recipientId, NotificationChannelType channelType, String address) {
        return createNotification(title, message, NotificationCategory.CRITICAL_ALERT, channelType, address, recipientId);
    }
    
    /**
     * Создать WebSocket уведомление для real-time обновлений
     */
    public NotificationId createWebSocketNotification(String title, String message, UserId recipientId, String sessionId) {
        return createNotification(title, message, NotificationCategory.WEBSOCKET, NotificationChannelType.WEBSOCKET, sessionId, recipientId);
    }
    
    /**
     * Создать Telegram уведомление
     */
    public NotificationId createTelegramNotification(String title, String message, UserId recipientId, String chatId) {
        return createNotification(title, message, NotificationCategory.TELEGRAM, NotificationChannelType.TELEGRAM, chatId, recipientId);
    }
} 
