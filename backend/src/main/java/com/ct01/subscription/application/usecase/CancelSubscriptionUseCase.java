package com.ct01.subscription.application.usecase;

import com.ct01.core.application.UseCase;
import com.ct01.shared.exception.DomainException;
import com.ct01.subscription.application.command.CancelSubscriptionCommand;
import com.ct01.subscription.domain.Subscription;
import com.ct01.subscription.domain.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case для отмены подписки
 */
@Service
@Transactional
public class CancelSubscriptionUseCase implements UseCase<CancelSubscriptionCommand, Void> {
    
    private final SubscriptionRepository subscriptionRepository;
    
    public CancelSubscriptionUseCase(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }
    
    @Override
    public Void execute(CancelSubscriptionCommand command) {
        Subscription subscription = subscriptionRepository.findById(command.subscriptionId())
            .orElseThrow(() -> DomainException.aggregateNotFound("Subscription", command.subscriptionId()));
        
        subscription.cancel(command.reason());
        subscriptionRepository.save(subscription);
        
        return null;
    }
} 
