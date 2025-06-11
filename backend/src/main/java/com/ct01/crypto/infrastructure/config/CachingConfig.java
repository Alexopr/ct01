package com.ct01.crypto.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Конфигурация кеширования для crypto модуля
 * Настройка Redis и локального кеширования для различных типов данных
 */
@Configuration
@EnableCaching
public class CachingConfig {

    /**
     * Имена кешей для crypto модуля
     */
    public static final class CacheNames {
        public static final String ACTIVE_COINS = "activeCoins";
        public static final String COIN_BY_SYMBOL = "coinBySymbol";
        public static final String COIN_BY_ID = "coinById";
        public static final String TOP_RANKED_COINS = "topRankedCoins";
        public static final String TRACKED_COINS_ACTIVE = "trackedCoinsActive";
        public static final String TRACKED_COIN_BY_SYMBOL = "trackedCoinBySymbol";
        public static final String EXCHANGE_DATA = "exchangeData";
        public static final String LATEST_PRICES = "latestPrices";
        public static final String PRICE_STATISTICS = "priceStatistics";
        public static final String MARKET_DATA = "marketData";
        public static final String USER_PREFERENCES = "userPreferences";
        
        // Специальные кеши для различных TTL
        public static final String STATIC_DATA = "staticData";        // 24 часа
        public static final String FREQUENT_DATA = "frequentData";    // 5 минут
        public static final String REALTIME_DATA = "realtimeData";    // 30 секунд
    }

    /**
     * Основной Redis Cache Manager
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.redis.host")
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        
        // Базовая конфигурация для Redis кеша
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(15)) // TTL по умолчанию: 15 минут
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues()
                .prefixCacheNameWith("ct01:crypto:");

        // Специфичные конфигурации для разных типов данных
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Статические данные (биржи, конфигурации) - кешируем на 24 часа
        cacheConfigurations.put(CacheNames.STATIC_DATA, defaultConfig.entryTtl(Duration.ofHours(24)));
        cacheConfigurations.put(CacheNames.EXCHANGE_DATA, defaultConfig.entryTtl(Duration.ofHours(24)));
        
        // Информация о монетах - кешируем на 2 часа (относительно статичная)
        cacheConfigurations.put(CacheNames.COIN_BY_ID, defaultConfig.entryTtl(Duration.ofHours(2)));
        cacheConfigurations.put(CacheNames.COIN_BY_SYMBOL, defaultConfig.entryTtl(Duration.ofHours(2)));
        cacheConfigurations.put(CacheNames.ACTIVE_COINS, defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put(CacheNames.TOP_RANKED_COINS, defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Отслеживаемые монеты - кешируем на 30 минут
        cacheConfigurations.put(CacheNames.TRACKED_COINS_ACTIVE, defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put(CacheNames.TRACKED_COIN_BY_SYMBOL, defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // Часто обновляемые данные - кешируем на 5 минут
        cacheConfigurations.put(CacheNames.FREQUENT_DATA, defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put(CacheNames.MARKET_DATA, defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put(CacheNames.PRICE_STATISTICS, defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // Реалтайм данные - кешируем на 30 секунд
        cacheConfigurations.put(CacheNames.REALTIME_DATA, defaultConfig.entryTtl(Duration.ofSeconds(30)));
        cacheConfigurations.put(CacheNames.LATEST_PRICES, defaultConfig.entryTtl(Duration.ofSeconds(30)));
        
        // Пользовательские настройки - кешируем на 1 час
        cacheConfigurations.put(CacheNames.USER_PREFERENCES, defaultConfig.entryTtl(Duration.ofHours(1)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    /**
     * Fallback Cache Manager для случаев когда Redis недоступен
     */
    @Bean
    @ConditionalOnProperty(name = "spring.redis.host", havingValue = "", matchIfMissing = true)
    public CacheManager concurrentMapCacheManager() {
        return new ConcurrentMapCacheManager(
                CacheNames.ACTIVE_COINS,
                CacheNames.COIN_BY_SYMBOL,
                CacheNames.COIN_BY_ID,
                CacheNames.TOP_RANKED_COINS,
                CacheNames.TRACKED_COINS_ACTIVE,
                CacheNames.TRACKED_COIN_BY_SYMBOL,
                CacheNames.EXCHANGE_DATA,
                CacheNames.LATEST_PRICES,
                CacheNames.PRICE_STATISTICS,
                CacheNames.MARKET_DATA,
                CacheNames.USER_PREFERENCES,
                CacheNames.STATIC_DATA,
                CacheNames.FREQUENT_DATA,
                CacheNames.REALTIME_DATA
        );
    }

    /**
     * Кеш-менеджер для небольших локальных кешей (как дополнение к Redis)
     */
    @Bean("localCacheManager")
    public CacheManager localCacheManager() {
        return new ConcurrentMapCacheManager(
                "localActiveCoins",     // Локальная копия активных монет
                "localExchangeConfig",  // Локальная конфигурация бирж
                "localUserSession"      // Сессионные данные пользователя
        );
    }
}

/**
 * Утилиты для работы с кешированием
 */
class CacheUtils {
    
    /**
     * Генерация ключа кеша для монеты по символу
     */
    public static String coinSymbolKey(String symbol) {
        return "coin:symbol:" + symbol.toLowerCase();
    }
    
    /**
     * Генерация ключа кеша для последней цены
     */
    public static String latestPriceKey(String coinSymbol, String exchangeName) {
        return String.format("price:latest:%s:%s", 
                coinSymbol.toLowerCase(), 
                exchangeName.toLowerCase());
    }
    
    /**
     * Генерация ключа кеша для отслеживаемой монеты
     */
    public static String trackedCoinKey(String symbol) {
        return "tracked:symbol:" + symbol.toLowerCase();
    }
    
    /**
     * Генерация ключа кеша для топ монет
     */
    public static String topCoinsKey(int limit) {
        return "coins:top:" + limit;
    }
    
    /**
     * Генерация ключа кеша для пользовательских настроек
     */
    public static String userPreferencesKey(Long userId) {
        return "user:prefs:" + userId;
    }
} 
