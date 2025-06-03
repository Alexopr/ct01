package alg.coyote001.service;

import alg.coyote001.dto.TrackedCoinDto;
import alg.coyote001.entity.TrackedCoin;
import alg.coyote001.repository.TrackedCoinRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing tracked cryptocurrency coins
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CoinTrackingService {
    
    private final TrackedCoinRepository trackedCoinRepository;
    
    /**
     * Create a new tracked coin
     */
    @CacheEvict(value = {"tracked-coins", "active-coins"}, allEntries = true)
    public TrackedCoinDto createTrackedCoin(TrackedCoinDto coinDto) {
        log.info("Creating new tracked coin: {}", coinDto.getSymbol());
        
        // Validate unique symbol
        if (trackedCoinRepository.existsBySymbolIgnoreCase(coinDto.getSymbol())) {
            throw new IllegalArgumentException("Coin with symbol " + coinDto.getSymbol() + " already exists");
        }
        
        // Normalize symbol to uppercase
        coinDto.setSymbol(coinDto.getSymbol().toUpperCase());
        
        // Normalize quote currencies to uppercase
        Set<String> normalizedQuotes = coinDto.getQuoteCurrencies().stream()
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
        coinDto.setQuoteCurrencies(normalizedQuotes);
        
        TrackedCoin savedCoin = trackedCoinRepository.save(coinDto.toEntity());
        
        log.info("Successfully created tracked coin with ID: {} for symbol: {}", 
                savedCoin.getId(), savedCoin.getSymbol());
        
        return TrackedCoinDto.fromEntity(savedCoin);
    }
    
    /**
     * Get tracked coin by ID
     */
    @Cacheable(value = "tracked-coins", key = "#id")
    @Transactional(readOnly = true)
    public Optional<TrackedCoinDto> getTrackedCoin(Long id) {
        log.debug("Fetching tracked coin by ID: {}", id);
        return trackedCoinRepository.findById(id)
                .map(TrackedCoinDto::fromEntity);
    }
    
    /**
     * Get tracked coin by symbol
     */
    @Cacheable(value = "tracked-coins", key = "#symbol.toLowerCase()")
    @Transactional(readOnly = true)
    public Optional<TrackedCoinDto> getTrackedCoinBySymbol(String symbol) {
        log.debug("Fetching tracked coin by symbol: {}", symbol);
        return trackedCoinRepository.findBySymbolIgnoreCase(symbol)
                .map(TrackedCoinDto::fromEntity);
    }
    
    /**
     * Get all active tracked coins
     */
    @Cacheable(value = "active-coins")
    @Transactional(readOnly = true)
    public List<TrackedCoinDto> getActiveTrackedCoins() {
        log.debug("Fetching all active tracked coins");
        return trackedCoinRepository.findByIsActiveTrueOrderByPriorityDesc()
                .stream()
                .map(TrackedCoinDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get tracked coins by exchange
     */
    @Transactional(readOnly = true)
    public List<TrackedCoinDto> getTrackedCoinsByExchange(TrackedCoin.Exchange exchange) {
        log.debug("Fetching tracked coins for exchange: {}", exchange);
        return trackedCoinRepository.findByExchange(exchange)
                .stream()
                .map(TrackedCoinDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get tracked coins with WebSocket enabled
     */
    @Transactional(readOnly = true)
    public List<TrackedCoinDto> getWebSocketEnabledCoins() {
        log.debug("Fetching WebSocket enabled tracked coins");
        return trackedCoinRepository.findByWebsocketEnabledTrueAndIsActiveTrue()
                .stream()
                .map(TrackedCoinDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get tracked coins by quote currency
     */
    @Transactional(readOnly = true)
    public List<TrackedCoinDto> getTrackedCoinsByQuoteCurrency(String quoteCurrency) {
        log.debug("Fetching tracked coins for quote currency: {}", quoteCurrency);
        return trackedCoinRepository.findByQuoteCurrency(quoteCurrency.toUpperCase())
                .stream()
                .map(TrackedCoinDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all tracked coins with pagination
     */
    @Transactional(readOnly = true)
    public Page<TrackedCoinDto> getAllTrackedCoins(Pageable pageable) {
        log.debug("Fetching tracked coins with pagination: {}", pageable);
        return trackedCoinRepository.findAll(pageable)
                .map(TrackedCoinDto::fromEntity);
    }
    
    /**
     * Update tracked coin
     */
    @CacheEvict(value = {"tracked-coins", "active-coins"}, allEntries = true)
    public TrackedCoinDto updateTrackedCoin(Long id, TrackedCoinDto coinDto) {
        log.info("Updating tracked coin with ID: {}", id);
        
        TrackedCoin existingCoin = trackedCoinRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tracked coin not found with ID: " + id));
        
        // Check if symbol is being changed and validate uniqueness
        if (!existingCoin.getSymbol().equalsIgnoreCase(coinDto.getSymbol())) {
            if (trackedCoinRepository.existsBySymbolIgnoreCase(coinDto.getSymbol())) {
                throw new IllegalArgumentException("Coin with symbol " + coinDto.getSymbol() + " already exists");
            }
        }
        
        // Update fields
        existingCoin.setSymbol(coinDto.getSymbol().toUpperCase());
        existingCoin.setName(coinDto.getName());
        existingCoin.setExchanges(coinDto.getExchanges());
        existingCoin.setQuoteCurrencies(coinDto.getQuoteCurrencies().stream()
                .map(String::toUpperCase)
                .collect(Collectors.toSet()));
        existingCoin.setIsActive(coinDto.getIsActive());
        existingCoin.setPollingIntervalSeconds(coinDto.getPollingIntervalSeconds());
        existingCoin.setWebsocketEnabled(coinDto.getWebsocketEnabled());
        existingCoin.setPriority(coinDto.getPriority());
        existingCoin.setNotes(coinDto.getNotes());
        
        TrackedCoin savedCoin = trackedCoinRepository.save(existingCoin);
        
        log.info("Successfully updated tracked coin: {}", savedCoin.getSymbol());
        return TrackedCoinDto.fromEntity(savedCoin);
    }
    
    /**
     * Delete tracked coin
     */
    @CacheEvict(value = {"tracked-coins", "active-coins"}, allEntries = true)
    public void deleteTrackedCoin(Long id) {
        log.info("Deleting tracked coin with ID: {}", id);
        
        if (!trackedCoinRepository.existsById(id)) {
            throw new IllegalArgumentException("Tracked coin not found with ID: " + id);
        }
        
        trackedCoinRepository.deleteById(id);
        log.info("Successfully deleted tracked coin with ID: {}", id);
    }
    
    /**
     * Activate/Deactivate tracked coin
     */
    @CacheEvict(value = {"tracked-coins", "active-coins"}, allEntries = true)
    public TrackedCoinDto toggleActivation(Long id, boolean isActive) {
        log.info("Toggling activation for tracked coin ID: {} to: {}", id, isActive);
        
        TrackedCoin coin = trackedCoinRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tracked coin not found with ID: " + id));
        
        coin.setIsActive(isActive);
        TrackedCoin savedCoin = trackedCoinRepository.save(coin);
        
        log.info("Successfully updated activation status for coin: {} to: {}", 
                savedCoin.getSymbol(), isActive);
        
        return TrackedCoinDto.fromEntity(savedCoin);
    }
    
    /**
     * Get coins with custom polling intervals
     */
    @Transactional(readOnly = true)
    public List<TrackedCoinDto> getCoinsWithCustomPolling() {
        log.debug("Fetching coins with custom polling intervals");
        return trackedCoinRepository.findByPollingIntervalSecondsIsNotNullAndIsActiveTrue()
                .stream()
                .map(TrackedCoinDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get coins by priority range
     */
    @Transactional(readOnly = true)
    public List<TrackedCoinDto> getCoinsByPriority(int minPriority) {
        log.debug("Fetching coins with priority >= {}", minPriority);
        return trackedCoinRepository.findByPriorityGreaterThanEqualAndIsActiveTrueOrderByPriorityDesc(minPriority)
                .stream()
                .map(TrackedCoinDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get statistics about tracked coins
     */
    @Transactional(readOnly = true)
    public TrackingStats getTrackingStats() {
        log.debug("Fetching tracking statistics");
        
        Long totalActive = trackedCoinRepository.countActiveCoins();
        Long binanceCount = trackedCoinRepository.countByExchange(TrackedCoin.Exchange.BINANCE);
        Long bybitCount = trackedCoinRepository.countByExchange(TrackedCoin.Exchange.BYBIT);
        Long okxCount = trackedCoinRepository.countByExchange(TrackedCoin.Exchange.OKX);
        
        return TrackingStats.builder()
                .totalActiveCoins(totalActive)
                .binanceCoins(binanceCount)
                .bybitCoins(bybitCount)
                .okxCoins(okxCount)
                .build();
    }
    
    /**
     * Bulk activate/deactivate coins by symbols
     */
    @CacheEvict(value = {"tracked-coins", "active-coins"}, allEntries = true)
    public List<TrackedCoinDto> bulkToggleActivation(Set<String> symbols, boolean isActive) {
        log.info("Bulk toggling activation for {} symbols to: {}", symbols.size(), isActive);
        
        List<TrackedCoin> coins = trackedCoinRepository.findBySymbolIgnoreCaseInAndIsActiveTrue(symbols);
        
        coins.forEach(coin -> coin.setIsActive(isActive));
        List<TrackedCoin> savedCoins = trackedCoinRepository.saveAll(coins);
        
        log.info("Successfully updated activation status for {} coins", savedCoins.size());
        
        return savedCoins.stream()
                .map(TrackedCoinDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Statistics DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TrackingStats {
        private Long totalActiveCoins;
        private Long binanceCoins;
        private Long bybitCoins;
        private Long okxCoins;
    }
} 