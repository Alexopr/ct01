package com.ct01.crypto.infrastructure.mapper;

import com.ct01.crypto.domain.TrackedCoin;
import com.ct01.crypto.infrastructure.persistence.TrackedCoinEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Маппер между доменной моделью TrackedCoin и DDD TrackedCoinEntity
 */
@Component
public class TrackedCoinMapper {
    
    /**
     * Преобразовать Entity в доменную модель
     */
    public TrackedCoin toDomain(TrackedCoinEntity entity) {
        if (entity == null) {
            return null;
        }
        
        Set<TrackedCoin.Exchange> exchanges = mapExchangesToDomain(entity.getExchanges());
        
        return new TrackedCoin(
            entity.getId(),
            entity.getSymbol(),
            entity.getName(),
            exchanges,
            entity.getQuoteCurrencies(),
            entity.getIsActive(),
            entity.getPollingIntervalSeconds(),
            entity.getWebsocketEnabled(),
            entity.getPriority(),
            entity.getNotes(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
    
    /**
     * Преобразовать доменную модель в Entity
     */
    public TrackedCoinEntity toEntity(TrackedCoin domain) {
        if (domain == null) {
            return null;
        }
        
        Set<TrackedCoinEntity.Exchange> exchanges = mapExchangesToEntity(domain.getExchanges());
        
        return TrackedCoinEntity.builder()
                .id(domain.getId())
                .symbol(domain.getSymbol())
                .name(domain.getName())
                .exchanges(exchanges)
                .quoteCurrencies(domain.getQuoteCurrencies())
                .isActive(domain.getIsActive())
                .pollingIntervalSeconds(domain.getPollingIntervalSeconds())
                .websocketEnabled(domain.getWebsocketEnabled())
                .priority(domain.getPriority())
                .notes(domain.getNotes())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
    
    /**
     * Преобразовать список Entity в список доменных моделей
     */
    public List<TrackedCoin> toDomainList(List<TrackedCoinEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        
        return entities.stream()
                .map(this::toDomain)
                .toList();
    }
    
    /**
     * Преобразовать список доменных моделей в список Entity
     */
    public List<TrackedCoinEntity> toEntityList(List<TrackedCoin> domains) {
        if (domains == null) {
            return List.of();
        }
        
        return domains.stream()
                .map(this::toEntity)
                .toList();
    }
    
    /**
     * Маппинг бирж из Entity в домен
     */
    private Set<TrackedCoin.Exchange> mapExchangesToDomain(Set<TrackedCoinEntity.Exchange> entityExchanges) {
        if (entityExchanges == null) {
            return Set.of();
        }
        
        return entityExchanges.stream()
                .map(this::mapExchangeToDomain)
                .collect(Collectors.toSet());
    }
    
    /**
     * Маппинг бирж из домена в Entity
     */
    private Set<TrackedCoinEntity.Exchange> mapExchangesToEntity(Set<TrackedCoin.Exchange> domainExchanges) {
        if (domainExchanges == null) {
            return Set.of();
        }
        
        return domainExchanges.stream()
                .map(this::mapExchangeToEntity)
                .collect(Collectors.toSet());
    }
    
    /**
     * Маппинг одной биржи из Entity в домен
     */
    private TrackedCoin.Exchange mapExchangeToDomain(TrackedCoinEntity.Exchange entityExchange) {
        if (entityExchange == null) {
            return null;
        }
        
        return switch (entityExchange) {
            case BINANCE -> TrackedCoin.Exchange.BINANCE;
            case BYBIT -> TrackedCoin.Exchange.BYBIT;
            case OKX -> TrackedCoin.Exchange.OKX;
        };
    }
    
    /**
     * Маппинг одной биржи из домена в Entity
     */
    private TrackedCoinEntity.Exchange mapExchangeToEntity(TrackedCoin.Exchange domainExchange) {
        if (domainExchange == null) {
            return null;
        }
        
        return switch (domainExchange) {
            case BINANCE -> TrackedCoinEntity.Exchange.BINANCE;
            case BYBIT -> TrackedCoinEntity.Exchange.BYBIT;
            case OKX -> TrackedCoinEntity.Exchange.OKX;
        };
    }
} 
