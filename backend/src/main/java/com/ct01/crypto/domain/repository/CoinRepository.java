package com.ct01.crypto.domain.repository;

import com.ct01.crypto.domain.Coin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Доменный репозиторий для работы с криптовалютами
 */
public interface CoinRepository {
    
    /**
     * Найти монету по ID
     */
    Optional<Coin> findById(Long id);
    
    /**
     * Найти монету по символу
     */
    Optional<Coin> findBySymbol(String symbol);
    
    /**
     * Найти активные монеты
     */
    List<Coin> findActiveCoins();
    
    /**
     * Найти монеты по приоритету
     */
    List<Coin> findByPriorityGreaterThanEqual(Integer priority);
    
    /**
     * Поиск монет по символу (частичное совпадение)
     */
    List<Coin> findBySymbolContaining(String symbolPattern);
    
    /**
     * Поиск монет по названию (частичное совпадение)
     */
    List<Coin> findByNameContaining(String namePattern);
    
    /**
     * Получить все монеты с пагинацией
     */
    Page<Coin> findAll(Pageable pageable);
    
    /**
     * Получить популярные монеты (по рейтингу)
     */
    List<Coin> findTopRankedCoins(int limit);
    
    /**
     * Проверить существование монеты по символу
     */
    boolean existsBySymbol(String symbol);
    
    /**
     * Сохранить монету
     */
    Coin save(Coin coin);
    
    /**
     * Удалить монету
     */
    void delete(Coin coin);
    
    /**
     * Подсчитать количество активных монет
     */
    long countActiveCoins();
} 
