package com.ct01.notification.domain;

import com.ct01.core.domain.UserId;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Domain Service для бизнес-логики уведомлений
 */
public class NotificationDomainService {
    
    private final NotificationRepository notificationRepository;
    
    public NotificationDomainService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }
    
    /**
     * Создать уведомление с автоматическим определением приоритета
     */
    public Notification createNotification(
            NotificationContent content,
            NotificationChannel channel,
            UserId recipientId) {
        
        NotificationPriority priority = determinePriority(content);
        LocalDateTime expiresAt = calculateExpirationTime(content.getCategory(), priority);
        
        return new Notification(
                NotificationId.generate(),
                content,
                channel,
                recipientId,
                priority,
                expiresAt
        );
    }
    
    /**
     * Определить приоритет на основе содержимого
     */
    public NotificationPriority determinePriority(NotificationContent content) {
        if (content.isUrgent()) {
            return NotificationPriority.URGENT;
        }
        
        if (content.getCategory().requiresHighPriority()) {
            return NotificationPriority.HIGH;
        }
        
        return NotificationPriority.NORMAL;
    }
    
    /**
     * Вычислить время истечения уведомления
     */
    public LocalDateTime calculateExpirationTime(NotificationCategory category, NotificationPriority priority) {
        int defaultHours = category.getDefaultTtlHours();
        
        // Корректируем время истечения в зависимости от приоритета
        int adjustedHours = switch (priority) {
            case URGENT -> Math.max(1, defaultHours / 4); // Минимум 1 час
            case HIGH -> Math.max(2, defaultHours / 2); // Минимум 2 часа
            case NORMAL -> defaultHours;
            case LOW -> defaultHours * 2;
        };
        
        return LocalDateTime.now().plusHours(adjustedHours);
    }
    
    /**
     * Найти уведомления готовые к отправке, отсортированные по приоритету
     */
    public List<Notification> findNotificationsReadyForDelivery() {
        List<Notification> pending = notificationRepository.findPendingForDelivery();
        
        return pending.stream()
                .filter(Notification::canBeSent)
                .sorted(Comparator
                        .comparing((Notification n) -> n.getPriority().getLevel()).reversed()
                        .thenComparing(Notification::getCreatedAt))
                .collect(Collectors.toList());
    }
    
    /**
     * Найти уведомления для повтора отправки
     */
    public List<Notification> findNotificationsForRetry() {
        return notificationRepository.findForRetry(5) // Максимум 5 попыток
                .stream()
                .filter(notification -> {
                    long waitTime = notification.getRetryDelayMinutes();
                    LocalDateTime nextRetryTime = notification.getCreatedAt().plusMinutes(waitTime * notification.getRetryCount());
                    return LocalDateTime.now().isAfter(nextRetryTime);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Пометить истекшие уведомления
     */
    public void markExpiredNotifications() {
        List<Notification> expired = notificationRepository.findExpired();
        
        for (Notification notification : expired) {
            if (notification.getStatus() == NotificationStatus.PENDING) {
                notification.markAsFailed("Уведомление истекло");
                notificationRepository.save(notification);
            }
        }
    }
    
    /**
     * Получить статистику уведомлений пользователя
     */
    public NotificationStatistics getUserNotificationStatistics(UserId userId) {
        List<Notification> userNotifications = notificationRepository.findByRecipientId(userId);
        
        Map<NotificationStatus, Long> statusCounts = userNotifications.stream()
                .collect(Collectors.groupingBy(
                        Notification::getStatus,
                        Collectors.counting()
                ));
        
        Map<NotificationCategory, Long> categoryCounts = userNotifications.stream()
                .collect(Collectors.groupingBy(
                        n -> n.getContent().getCategory(),
                        Collectors.counting()
                ));
        
        long unreadCount = notificationRepository.countUnreadByRecipientId(userId);
        
        return new NotificationStatistics(statusCounts, categoryCounts, unreadCount);
    }
    
    /**
     * Определить подходящий канал для уведомления
     */
    public NotificationChannelType determineBestChannel(
            NotificationContent content, 
            List<NotificationChannelType> availableChannels) {
        
        NotificationChannelType[] recommended = content.getCategory().getRecommendedChannels();
        
        // Найти первый доступный рекомендованный канал
        for (NotificationChannelType recommendedChannel : recommended) {
            if (availableChannels.contains(recommendedChannel)) {
                return recommendedChannel;
            }
        }
        
        // Если нет рекомендованных, выбрать канал с наивысшим приоритетом
        return availableChannels.stream()
                .max(Comparator.comparing(NotificationChannelType::getDeliveryPriority))
                .orElse(NotificationChannelType.EMAIL);
    }
    
    /**
     * Проверить лимиты уведомлений для пользователя
     */
    public boolean canSendNotification(UserId userId, NotificationCategory category) {
        // Проверить дневной лимит (можно вынести в конфигурацию)
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        List<Notification> todayNotifications = notificationRepository.findByCreatedAtBetween(startOfDay, endOfDay)
                .stream()
                .filter(n -> n.getRecipientId().equals(userId))
                .filter(n -> n.getContent().getCategory() == category)
                .collect(Collectors.toList());
        
        // Лимиты по категориям (можно вынести в конфигурацию)
        int dailyLimit = switch (category) {
            case SECURITY, CRITICAL_ALERT -> 50; // Без ограничений для критичных
            case PRICE_ALERT, VOLUME_ALERT -> 20;
            case MARKETING -> 3;
            default -> 10;
        };
        
        return todayNotifications.size() < dailyLimit;
    }
    
    /**
     * Очистить старые уведомления
     */
    public void cleanupOldNotifications(int daysToKeep) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(daysToKeep);
        notificationRepository.deleteOlderThan(threshold);
    }
    
    /**
     * Статистика уведомлений
     */
    public static class NotificationStatistics {
        private final Map<NotificationStatus, Long> statusCounts;
        private final Map<NotificationCategory, Long> categoryCounts;
        private final long unreadCount;
        
        public NotificationStatistics(
                Map<NotificationStatus, Long> statusCounts,
                Map<NotificationCategory, Long> categoryCounts,
                long unreadCount) {
            this.statusCounts = statusCounts;
            this.categoryCounts = categoryCounts;
            this.unreadCount = unreadCount;
        }
        
        public Map<NotificationStatus, Long> getStatusCounts() { return statusCounts; }
        public Map<NotificationCategory, Long> getCategoryCounts() { return categoryCounts; }
        public long getUnreadCount() { return unreadCount; }
        
        public long getTotalCount() {
            return statusCounts.values().stream().mapToLong(Long::longValue).sum();
        }
    }
}
