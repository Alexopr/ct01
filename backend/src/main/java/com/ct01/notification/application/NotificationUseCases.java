package com.ct01.notification.application;

import com.ct01.notification.domain.*;
import com.ct01.core.domain.UserId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Use Cases для работы с уведомлениями
 */
public class NotificationUseCases {
    
    /**
     * Use Case для создания уведомления
     */
    public static class CreateNotificationUseCase {
        
        private final NotificationRepository notificationRepository;
        private final NotificationDomainService domainService;
        
        public CreateNotificationUseCase(NotificationRepository notificationRepository, NotificationDomainService domainService) {
            this.notificationRepository = notificationRepository;
            this.domainService = domainService;
        }
        
        public NotificationId execute(CreateNotificationCommand command) {
            // Проверить лимиты
            if (!domainService.canSendNotification(command.recipientId(), command.content().getCategory())) {
                throw new NotificationLimitExceededException("Превышен дневной лимит уведомлений для категории: " + command.content().getCategory());
            }
            
            // Создать уведомление
            Notification notification = domainService.createNotification(
                command.content(),
                command.channel(), 
                command.recipientId()
            );
            
            // Сохранить
            Notification saved = notificationRepository.save(notification);
            
            return saved.getId();
        }
        
        public record CreateNotificationCommand(
            NotificationContent content,
            NotificationChannel channel,
            UserId recipientId
        ) {}
    }
    
    /**
     * Use Case для отправки уведомления
     */
    public static class SendNotificationUseCase {
        
        private final NotificationRepository notificationRepository;
        
        public SendNotificationUseCase(NotificationRepository notificationRepository) {
            this.notificationRepository = notificationRepository;
        }
        
        public void execute(SendNotificationCommand command) {
            Optional<Notification> optionalNotification = notificationRepository.findById(command.notificationId());
            
            if (optionalNotification.isEmpty()) {
                throw new NotificationNotFoundException("Уведомление не найдено: " + command.notificationId());
            }
            
            Notification notification = optionalNotification.get();
            
            if (!notification.canBeSent()) {
                throw new NotificationCannotBeSentException("Уведомление не может быть отправлено: " + notification.getStatus());
            }
            
            // Пометить как отправленное
            notification.markAsSent();
            notificationRepository.save(notification);
        }
        
        public record SendNotificationCommand(NotificationId notificationId) {}
    }
    
    /**
     * Use Case для получения уведомлений пользователя
     */
    public static class GetUserNotificationsUseCase {
        
        private final NotificationRepository notificationRepository;
        
        public GetUserNotificationsUseCase(NotificationRepository notificationRepository) {
            this.notificationRepository = notificationRepository;
        }
        
        public List<Notification> execute(GetUserNotificationsQuery query) {
            if (query.status() != null) {
                return notificationRepository.findByRecipientIdAndStatus(query.userId(), query.status());
            }
            
            if (query.category() != null) {
                return notificationRepository.findByRecipientIdAndCategory(query.userId(), query.category());
            }
            
            if (query.onlyUnread()) {
                return notificationRepository.findUnreadByRecipientId(query.userId());
            }
            
            if (query.page() != null && query.size() != null) {
                return notificationRepository.findByRecipientIdWithPagination(query.userId(), query.page(), query.size());
            }
            
            return notificationRepository.findByRecipientId(query.userId());
        }
        
        public record GetUserNotificationsQuery(
            UserId userId,
            NotificationStatus status,
            NotificationCategory category,
            boolean onlyUnread,
            Integer page,
            Integer size
        ) {
            public GetUserNotificationsQuery(UserId userId) {
                this(userId, null, null, false, null, null);
            }
        }
    }
    
    /**
     * Use Case для пометки уведомления как прочитанного
     */
    public static class MarkNotificationAsReadUseCase {
        
        private final NotificationRepository notificationRepository;
        
        public MarkNotificationAsReadUseCase(NotificationRepository notificationRepository) {
            this.notificationRepository = notificationRepository;
        }
        
        public void execute(MarkAsReadCommand command) {
            Optional<Notification> optionalNotification = notificationRepository.findById(command.notificationId());
            
            if (optionalNotification.isEmpty()) {
                throw new NotificationNotFoundException("Уведомление не найдено: " + command.notificationId());
            }
            
            Notification notification = optionalNotification.get();
            
            // Проверить права доступа
            if (!notification.getRecipientId().equals(command.userId())) {
                throw new NotificationAccessDeniedException("Нет доступа к уведомлению");
            }
            
            notification.markAsRead();
            notificationRepository.save(notification);
        }
        
        public record MarkAsReadCommand(NotificationId notificationId, UserId userId) {}
    }
    
    /**
     * Use Case для пометки всех уведомлений пользователя как прочитанных
     */
    public static class MarkAllNotificationsAsReadUseCase {
        
        private final NotificationRepository notificationRepository;
        
        public MarkAllNotificationsAsReadUseCase(NotificationRepository notificationRepository) {
            this.notificationRepository = notificationRepository;
        }
        
        public void execute(MarkAllAsReadCommand command) {
            notificationRepository.markAllAsReadForUser(command.userId());
        }
        
        public record MarkAllAsReadCommand(UserId userId) {}
    }
    
    /**
     * Use Case для получения статистики уведомлений
     */
    public static class GetNotificationStatisticsUseCase {
        
        private final NotificationDomainService domainService;
        
        public GetNotificationStatisticsUseCase(NotificationDomainService domainService) {
            this.domainService = domainService;
        }
        
        public NotificationDomainService.NotificationStatistics execute(GetStatisticsQuery query) {
            return domainService.getUserNotificationStatistics(query.userId());
        }
        
        public record GetStatisticsQuery(UserId userId) {}
    }
    
    /**
     * Use Case для обработки неудачных уведомлений
     */
    public static class HandleFailedNotificationUseCase {
        
        private final NotificationRepository notificationRepository;
        
        public HandleFailedNotificationUseCase(NotificationRepository notificationRepository) {
            this.notificationRepository = notificationRepository;
        }
        
        public void execute(HandleFailedCommand command) {
            Optional<Notification> optionalNotification = notificationRepository.findById(command.notificationId());
            
            if (optionalNotification.isEmpty()) {
                throw new NotificationNotFoundException("Уведомление не найдено: " + command.notificationId());
            }
            
            Notification notification = optionalNotification.get();
            
            if (notification.canRetry()) {
                notification.retry();
            } else {
                notification.markAsFailed(command.reason());
            }
            
            notificationRepository.save(notification);
        }
        
        public record HandleFailedCommand(NotificationId notificationId, String reason) {}
    }
    
    /**
     * Use Case для обработки доставленных уведомлений
     */
    public static class HandleDeliveredNotificationUseCase {
        
        private final NotificationRepository notificationRepository;
        
        public HandleDeliveredNotificationUseCase(NotificationRepository notificationRepository) {
            this.notificationRepository = notificationRepository;
        }
        
        public void execute(HandleDeliveredCommand command) {
            Optional<Notification> optionalNotification = notificationRepository.findById(command.notificationId());
            
            if (optionalNotification.isEmpty()) {
                throw new NotificationNotFoundException("Уведомление не найдено: " + command.notificationId());
            }
            
            Notification notification = optionalNotification.get();
            notification.markAsDelivered();
            notificationRepository.save(notification);
        }
        
        public record HandleDeliveredCommand(NotificationId notificationId) {}
    }
    
    /**
     * Use Case для получения уведомлений для отправки
     */
    public static class GetNotificationsForDeliveryUseCase {
        
        private final NotificationDomainService domainService;
        
        public GetNotificationsForDeliveryUseCase(NotificationDomainService domainService) {
            this.domainService = domainService;
        }
        
        public List<Notification> execute() {
            return domainService.findNotificationsReadyForDelivery();
        }
    }
    
    /**
     * Use Case для получения уведомлений для повтора
     */
    public static class GetNotificationsForRetryUseCase {
        
        private final NotificationDomainService domainService;
        
        public GetNotificationsForRetryUseCase(NotificationDomainService domainService) {
            this.domainService = domainService;
        }
        
        public List<Notification> execute() {
            return domainService.findNotificationsForRetry();
        }
    }
    
    /**
     * Use Case для очистки устаревших уведомлений
     */
    public static class CleanupExpiredNotificationsUseCase {
        
        private final NotificationDomainService domainService;
        
        public CleanupExpiredNotificationsUseCase(NotificationDomainService domainService) {
            this.domainService = domainService;
        }
        
        public void execute(CleanupCommand command) {
            domainService.markExpiredNotifications();
            domainService.cleanupOldNotifications(command.daysToKeep());
        }
        
        public record CleanupCommand(int daysToKeep) {
            public CleanupCommand() {
                this(30); // По умолчанию 30 дней
            }
        }
    }
    
    // Исключения
    public static class NotificationNotFoundException extends RuntimeException {
        public NotificationNotFoundException(String message) { super(message); }
    }
    
    public static class NotificationCannotBeSentException extends RuntimeException {
        public NotificationCannotBeSentException(String message) { super(message); }
    }
    
    public static class NotificationAccessDeniedException extends RuntimeException {
        public NotificationAccessDeniedException(String message) { super(message); }
    }
    
    public static class NotificationLimitExceededException extends RuntimeException {
        public NotificationLimitExceededException(String message) { super(message); }
    }
}
