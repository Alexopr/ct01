package alg.coyote001.service;

import alg.coyote001.dto.ExchangeDto;
import alg.coyote001.entity.Exchange;
import alg.coyote001.repository.ExchangeRepository;
import alg.coyote001.repository.PriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for exchange operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ExchangeService {
    
    private final ExchangeRepository exchangeRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final RateLimitingService rateLimitingService;
    
    /**
     * Get all exchanges with pagination
     */
    @Transactional(readOnly = true)
    public Page<ExchangeDto> getAllExchanges(Pageable pageable) {
        log.debug("Fetching all exchanges with pagination: {}", pageable);
        
        return exchangeRepository.findAll(pageable)
                .map(this::convertToDto);
    }
    
    /**
     * Get active exchanges only
     */
    @Transactional(readOnly = true)
    public List<ExchangeDto> getActiveExchanges() {
        log.debug("Fetching active exchanges");
        
        return exchangeRepository.findByIsActiveTrue()
                .stream()
                .map(this::convertToDto)
                .toList();
    }
    
    /**
     * Get exchange by name
     */
    @Transactional(readOnly = true)
    public Optional<ExchangeDto> getExchangeByName(String name) {
        log.debug("Fetching exchange by name: {}", name);
        
        return exchangeRepository.findByNameIgnoreCase(name)
                .map(this::convertToDto);
    }
    
    /**
     * Get exchange status and health information
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getExchangeStatus(String name) {
        log.debug("Fetching status for exchange: {}", name);
        
        Exchange exchange = exchangeRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new IllegalArgumentException("Exchange not found: " + name));
        
        // Get recent price updates count
        LocalDateTime lastHour = LocalDateTime.now().minusHours(1);
        Long recentUpdates = priceHistoryRepository.countByExchangeAndTimestampAfter(exchange, lastHour);
        
        // Check rate limiting status
        String rateLimitKey = "exchange:" + name.toLowerCase();
        boolean isRateLimited = rateLimitingService.isRateLimited(rateLimitKey);
        
        Map<String, Object> status = new HashMap<>();
        status.put("exchangeName", exchange.getName());
        status.put("isActive", exchange.getIsActive());
        status.put("apiUrl", exchange.getApiUrl());
        status.put("websocketUrl", exchange.getWebsocketUrl());
        status.put("recentUpdatesLastHour", recentUpdates);
        status.put("isRateLimited", isRateLimited);
        status.put("lastChecked", LocalDateTime.now());
        
        // Determine overall health status
        String healthStatus;
        if (!exchange.getIsActive()) {
            healthStatus = "INACTIVE";
        } else if (isRateLimited) {
            healthStatus = "RATE_LIMITED";
        } else if (recentUpdates > 0) {
            healthStatus = "HEALTHY";
        } else {
            healthStatus = "NO_DATA";
        }
        
        status.put("healthStatus", healthStatus);
        
        return status;
    }
    
    /**
     * Get supported trading pairs for an exchange
     */
    @Transactional(readOnly = true)
    public List<String> getSupportedTradingPairs(String name) {
        log.debug("Fetching supported pairs for exchange: {}", name);
        
        Exchange exchange = exchangeRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new IllegalArgumentException("Exchange not found: " + name));
        
        // Get unique trading pairs from price history
        List<String> pairs = priceHistoryRepository.findDistinctTradingPairsByExchange(exchange.getId());
        
        return pairs != null ? pairs : new ArrayList<>();
    }
    
    /**
     * Get exchange statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getExchangeStatistics(String name) {
        log.debug("Fetching statistics for exchange: {}", name);
        
        Exchange exchange = exchangeRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new IllegalArgumentException("Exchange not found: " + name));
        
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
        
        // Get statistics
        Long totalPriceUpdates = priceHistoryRepository.countByExchange(exchange);
        Long updatesLast24h = priceHistoryRepository.countByExchangeAndTimestampAfter(exchange, last24Hours);
        Long updatesLastWeek = priceHistoryRepository.countByExchangeAndTimestampAfter(exchange, lastWeek);
        Long uniqueCoins = priceHistoryRepository.countDistinctCoinsByExchange(exchange.getId());
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("exchangeName", exchange.getName());
        stats.put("totalPriceUpdates", totalPriceUpdates);
        stats.put("updatesLast24Hours", updatesLast24h);
        stats.put("updatesLastWeek", updatesLastWeek);
        stats.put("uniqueCoinsTracked", uniqueCoins);
        stats.put("isActive", exchange.getIsActive());
        stats.put("createdAt", exchange.getCreatedAt());
        stats.put("lastUpdated", exchange.getUpdatedAt());
        
        // Calculate average updates per hour (last 24h)
        double avgUpdatesPerHour = updatesLast24h / 24.0;
        stats.put("avgUpdatesPerHour", Math.round(avgUpdatesPerHour * 100.0) / 100.0);
        
        return stats;
    }
    
    /**
     * Get rate limit information for an exchange
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getRateLimitInfo(String name) {
        log.debug("Fetching rate limits for exchange: {}", name);
        
        Exchange exchange = exchangeRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new IllegalArgumentException("Exchange not found: " + name));
        
        String rateLimitKey = "exchange:" + name.toLowerCase();
        
        Map<String, Object> rateLimitInfo = new HashMap<>();
        rateLimitInfo.put("exchangeName", exchange.getName());
        rateLimitInfo.put("isRateLimited", rateLimitingService.isRateLimited(rateLimitKey));
        rateLimitInfo.put("remainingRequests", rateLimitingService.getRemainingRequests(rateLimitKey));
        rateLimitInfo.put("resetTime", rateLimitingService.getResetTime(rateLimitKey));
        rateLimitInfo.put("maxRequestsPerMinute", getRateLimitForExchange(name));
        rateLimitInfo.put("checkTime", LocalDateTime.now());
        
        return rateLimitInfo;
    }
    
    /**
     * Get rate limit configuration for a specific exchange
     */
    private int getRateLimitForExchange(String exchangeName) {
        // Default rate limits per exchange (requests per minute)
        return switch (exchangeName.toLowerCase()) {
            case "binance" -> 1200;
            case "bybit" -> 600;
            case "okx" -> 300;
            default -> 100;
        };
    }
    
    /**
     * Convert Exchange entity to DTO
     */
    private ExchangeDto convertToDto(Exchange exchange) {
        return ExchangeDto.builder()
                .id(exchange.getId())
                .name(exchange.getName())
                .apiUrl(exchange.getApiUrl())
                .websocketUrl(exchange.getWebsocketUrl())
                .isActive(exchange.getIsActive())
                .createdAt(exchange.getCreatedAt())
                .updatedAt(exchange.getUpdatedAt())
                .build();
    }
} 