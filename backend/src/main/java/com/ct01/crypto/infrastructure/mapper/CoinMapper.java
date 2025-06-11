package com.ct01.crypto.infrastructure.mapper;

import com.ct01.crypto.domain.Coin;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Маппер между доменной моделью Coin и существующей Entity
 */
@Component
public class CoinMapper {
    
    /**
     * Преобразовать Entity в доменную модель
     */
    public Coin toDomain(alg.coyote001.entity.Coin entity) {
        if (entity == null) {
            return null;
        }
        
        Coin.CoinStatus status = mapStatus(entity.getStatus());
        
        return new Coin(
            entity.getId(),
            entity.getSymbol(),
            entity.getName(),
            entity.getIconUrl(),
            entity.getDescription(),
            entity.getWebsiteUrl(),
            entity.getWhitepaperUrl(),
            entity.getMaxSupply(),
            entity.getCirculatingSupply(),
            entity.getMarketCap(),
            entity.getMarketRank(),
            status,
            entity.getPriority(),
            entity.getCategories(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getLastSyncAt()
        );
    }
    
    /**
     * Преобразовать доменную модель в Entity
     */
    public alg.coyote001.entity.Coin toEntity(Coin domain) {
        if (domain == null) {
            return null;
        }
        
        alg.coyote001.entity.Coin.CoinStatus entityStatus = mapStatusToEntity(domain.getStatus());
        
        return alg.coyote001.entity.Coin.builder()
                .id(domain.getId())
                .symbol(domain.getSymbol())
                .name(domain.getName())
                .iconUrl(domain.getIconUrl())
                .description(domain.getDescription())
                .websiteUrl(domain.getWebsiteUrl())
                .whitepaperUrl(domain.getWhitepaperUrl())
                .maxSupply(domain.getMaxSupply())
                .circulatingSupply(domain.getCirculatingSupply())
                .marketCap(domain.getMarketCap())
                .marketRank(domain.getMarketRank())
                .status(entityStatus)
                .priority(domain.getPriority())
                .categories(domain.getCategories())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .lastSyncAt(domain.getLastSyncAt())
                .build();
    }
    
    /**
     * Преобразовать список Entity в список доменных моделей
     */
    public List<Coin> toDomainList(List<alg.coyote001.entity.Coin> entities) {
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
    public List<alg.coyote001.entity.Coin> toEntityList(List<Coin> domains) {
        if (domains == null) {
            return List.of();
        }
        
        return domains.stream()
                .map(this::toEntity)
                .toList();
    }
    
    /**
     * Маппинг статуса из Entity в домен
     */
    private Coin.CoinStatus mapStatus(alg.coyote001.entity.Coin.CoinStatus entityStatus) {
        if (entityStatus == null) {
            return Coin.CoinStatus.INACTIVE;
        }
        
        return switch (entityStatus) {
            case ACTIVE -> Coin.CoinStatus.ACTIVE;
            case INACTIVE -> Coin.CoinStatus.INACTIVE;
            case DELISTED -> Coin.CoinStatus.DELISTED;
            case DEPRECATED -> Coin.CoinStatus.DEPRECATED;
        };
    }
    
    /**
     * Маппинг статуса из домена в Entity
     */
    private alg.coyote001.entity.Coin.CoinStatus mapStatusToEntity(Coin.CoinStatus domainStatus) {
        if (domainStatus == null) {
            return alg.coyote001.entity.Coin.CoinStatus.INACTIVE;
        }
        
        return switch (domainStatus) {
            case ACTIVE -> alg.coyote001.entity.Coin.CoinStatus.ACTIVE;
            case INACTIVE -> alg.coyote001.entity.Coin.CoinStatus.INACTIVE;
            case DELISTED -> alg.coyote001.entity.Coin.CoinStatus.DELISTED;
            case DEPRECATED -> alg.coyote001.entity.Coin.CoinStatus.DEPRECATED;
        };
    }
} 
