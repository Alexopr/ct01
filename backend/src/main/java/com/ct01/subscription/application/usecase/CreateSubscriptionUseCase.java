package com.ct01.subscription.application.usecase;

import com.ct01.core.application.UseCase;
import com.ct01.core.domain.Money;
import com.ct01.shared.exception.DomainException;
import com.ct01.subscription.application.command.CreateSubscriptionCommand;
import com.ct01.subscription.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case для создания подписки
 */
@Service
@Transactional
public class CreateSubscriptionUseCase implements UseCase<CreateSubscriptionCommand, SubscriptionId> {
    
    private final SubscriptionRepository subscriptionRepository;
    
    public CreateSubscriptionUseCase(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }
    
    @Override
    public SubscriptionId execute(CreateSubscriptionCommand command) {
        // Проверяем, нет ли уже активной подписки у пользователя
        if (subscriptionRepository.existsActiveByUserId(command.userId())) {
            throw DomainException.businessRule("У пользователя уже есть активная подписка");
        }
        
        // Создаем объекты Value Objects
        Money price = Money.of(command.price(), command.currency());
        BillingPeriod billingPeriod = BillingPeriod.fromString(command.billingCycle());
        
        // Создаем подписку
        Subscription subscription = Subscription.create(
            command.userId(),
            command.planType(),
            price,
            billingPeriod
        );
        
        // Сохраняем подписку
        Subscription savedSubscription = subscriptionRepository.save(subscription);
        
        return savedSubscription.getId();
    }
} 
