package com.ct01.crypto.infrastructure.mapper;

import com.ct01.crypto.domain.PriceHistory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Маппер между доменной моделью PriceHistory и существующей Entity
 */
@Component
public class PriceHistoryMapper {
    
    /**
     * Преобразовать Entity в доменную модель
     */
    public PriceHistory toDomain(alg.coyote001.entity.PriceHistory entity) {
        if (entity == null) {
            return null;
        }
        
        String coinSymbol = entity.getCoin() != null ? entity.getCoin().getSymbol() : null;
        String exchangeName = entity.getExchange() != null ? entity.getExchange().getName() : null;
        
        return new PriceHistory(
            entity.getId(),
            coinSymbol,
            exchangeName,
            entity.getTradingPair(),
            entity.getQuoteCurrency(),
            entity.getTimestamp(),
            entity.getOpenPrice(),
            entity.getHighPrice(),
            entity.getLowPrice(),
            entity.getClosePrice(),
            entity.getVolume(),
            entity.getVolumeUsd(),
            entity.getTradesCount(),
            entity.getPriceType()
        );
    }
    
    /**
     * Преобразовать доменную модель в Entity
     * Примечание: требует отдельного разрешения Coin и Exchange entities
     */
    public alg.coyote001.entity.PriceHistory toEntity(PriceHistory domain, 
                                                      alg.coyote001.entity.Coin coin,
                                                      alg.coyote001.entity.Exchange exchange) {
        if (domain == null) {
            return null;
        }
        
        return alg.coyote001.entity.PriceHistory.builder()
                .id(domain.getId())
                .coin(coin)
                .exchange(exchange)
                .tradingPair(domain.getTradingPair())
                .quoteCurrency(domain.getQuoteCurrency())
                .timestamp(domain.getTimestamp())
                .openPrice(domain.getOpenPrice())
                .highPrice(domain.getHighPrice())
                .lowPrice(domain.getLowPrice())
                .closePrice(domain.getClosePrice())
                .volume(domain.getVolume())
                .volumeUsd(domain.getVolumeUsd())
                .tradesCount(domain.getTradesCount())
                .priceType(domain.getPriceType())
                .build();
    }
    
    /**
     * Преобразовать список Entity в список доменных моделей
     */
    public List<PriceHistory> toDomainList(List<alg.coyote001.entity.PriceHistory> entities) {
        if (entities == null) {
            return List.of();
        }
        
        return entities.stream()
                .map(this::toDomain)
                .toList();
    }
    
    /**
     * Создать упрощенную Entity только с основными полями
     * Для случаев, когда не нужны полные связи
     */
    public alg.coyote001.entity.PriceHistory toSimpleEntity(PriceHistory domain) {
        if (domain == null) {
            return null;
        }
        
        return alg.coyote001.entity.PriceHistory.builder()
                .id(domain.getId())
                .tradingPair(domain.getTradingPair())
                .quoteCurrency(domain.getQuoteCurrency())
                .timestamp(domain.getTimestamp())
                .openPrice(domain.getOpenPrice())
                .highPrice(domain.getHighPrice())
                .lowPrice(domain.getLowPrice())
                .closePrice(domain.getClosePrice())
                .volume(domain.getVolume())
                .volumeUsd(domain.getVolumeUsd())
                .tradesCount(domain.getTradesCount())
                .priceType(domain.getPriceType())
                .build();
    }
} 
