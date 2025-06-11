package com.ct01.crypto.domain.repository;

import com.ct01.crypto.domain.TrackedCoin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Доменный репозиторий для работы с отслеживаемыми криптовалютами
 */
public interface TrackedCoinRepository {
    
    /**
     * Найти отслеживаемую монету по ID
     */
    Optional<TrackedCoin> findById(Long id);
    
    /**
     * Найти отслеживаемую монету по символу
     */
    Optional<TrackedCoin> findBySymbol(String symbol);
    
    /**
     * Найти все активные отслеживаемые монеты
     */
    List<TrackedCoin> findActiveCoins();
    
    /**
     * Найти активные отслеживаемые монеты по приоритету
     */
    List<TrackedCoin> findActiveCoinsByPriority();
    
    /**
     * Найти отслеживаемые монеты по бирже
     */
    List<TrackedCoin> findByExchange(TrackedCoin.Exchange exchange);
    
    /**
     * Найти отслеживаемые монеты с включенным WebSocket
     */
    List<TrackedCoin> findWebSocketEnabledCoins();
    
    /**
     * Найти отслеживаемые монеты по котируемой валюте
     */
    List<TrackedCoin> findByQuoteCurrency(String quoteCurrency);
    
    /**
     * Найти отслеживаемые монеты по списку символов
     */
    List<TrackedCoin> findBySymbolsIn(Set<String> symbols);
    
    /**
     * Найти отслеживаемые монеты с кастомным интервалом опроса
     */
    List<TrackedCoin> findWithCustomPollingInterval();
    
    /**
     * Найти отслеживаемые монеты по минимальному приоритету
     */
    List<TrackedCoin> findByMinPriority(Integer minPriority);
    
    /**
     * Получить все отслеживаемые монеты с пагинацией
     */
    Page<TrackedCoin> findAll(Pageable pageable);
    
    /**
     * Проверить существование отслеживаемой монеты по символу
     */
    boolean existsBySymbol(String symbol);
    
    /**
     * Сохранить отслеживаемую монету
     */
    TrackedCoin save(TrackedCoin trackedCoin);
    
    /**
     * Удалить отслеживаемую монету
     */
    void delete(TrackedCoin trackedCoin);
    
    /**
     * Подсчитать количество активных отслеживаемых монет
     */
    long countActiveCoins();
    
    /**
     * Подсчитать количество отслеживаемых монет по бирже
     */
    long countByExchange(TrackedCoin.Exchange exchange);
} 
