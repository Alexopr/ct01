package com.ct01.subscription.application;

import com.ct01.core.application.ApplicationFacade;
import com.ct01.core.domain.UserId;
import com.ct01.subscription.application.command.CancelSubscriptionCommand;
import com.ct01.subscription.application.command.CreateSubscriptionCommand;
import com.ct01.subscription.application.usecase.CancelSubscriptionUseCase;
import com.ct01.subscription.application.usecase.CreateSubscriptionUseCase;
import com.ct01.subscription.domain.Subscription;
import com.ct01.subscription.domain.SubscriptionId;
import com.ct01.subscription.domain.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Application Facade для Subscription Domain
 */
@Service
public class SubscriptionApplicationFacade implements ApplicationFacade {
    
    private final CreateSubscriptionUseCase createSubscriptionUseCase;
    private final CancelSubscriptionUseCase cancelSubscriptionUseCase;
    private final SubscriptionRepository subscriptionRepository;
    
    public SubscriptionApplicationFacade(
            CreateSubscriptionUseCase createSubscriptionUseCase,
            CancelSubscriptionUseCase cancelSubscriptionUseCase,
            SubscriptionRepository subscriptionRepository) {
        this.createSubscriptionUseCase = createSubscriptionUseCase;
        this.cancelSubscriptionUseCase = cancelSubscriptionUseCase;
        this.subscriptionRepository = subscriptionRepository;
    }
    
    /**
     * Создает новую подписку
     */
    public SubscriptionId createSubscription(CreateSubscriptionCommand command) {
        return createSubscriptionUseCase.execute(command);
    }
    
    /**
     * Отменяет подписку
     */
    public void cancelSubscription(CancelSubscriptionCommand command) {
        cancelSubscriptionUseCase.execute(command);
    }
    
    /**
     * Получает подписку по ID
     */
    public Optional<Subscription> getSubscription(SubscriptionId subscriptionId) {
        return subscriptionRepository.findById(subscriptionId);
    }
    
    /**
     * Получает активную подписку пользователя
     */
    public Optional<Subscription> getActiveSubscription(UserId userId) {
        return subscriptionRepository.findActiveByUserId(userId);
    }
    
    /**
     * Проверяет, есть ли у пользователя активная подписка
     */
    public boolean hasActiveSubscription(UserId userId) {
        return subscriptionRepository.existsActiveByUserId(userId);
    }
} 
