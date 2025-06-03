package alg.coyote001.controller;

import alg.coyote001.dto.TrackedCoinDto;
import alg.coyote001.entity.TrackedCoin;
import alg.coyote001.service.CoinTrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * REST Controller for managing tracked cryptocurrency coins
 */
@RestController
@RequestMapping("/api/v1/tracked-coins")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TrackedCoinController {
    
    private final CoinTrackingService coinTrackingService;
    
    /**
     * Create a new tracked coin
     */
    @PostMapping
    public ResponseEntity<TrackedCoinDto> createTrackedCoin(@Valid @RequestBody TrackedCoinDto coinDto) {
        log.info("Creating new tracked coin: {}", coinDto.getSymbol());
        
        try {
            TrackedCoinDto createdCoin = coinTrackingService.createTrackedCoin(coinDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCoin);
        } catch (IllegalArgumentException e) {
            log.error("Failed to create tracked coin: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get tracked coin by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TrackedCoinDto> getTrackedCoin(@PathVariable Long id) {
        log.debug("Fetching tracked coin by ID: {}", id);
        
        return coinTrackingService.getTrackedCoin(id)
                .map(coin -> ResponseEntity.ok(coin))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get tracked coin by symbol
     */
    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<TrackedCoinDto> getTrackedCoinBySymbol(@PathVariable String symbol) {
        log.debug("Fetching tracked coin by symbol: {}", symbol);
        
        return coinTrackingService.getTrackedCoinBySymbol(symbol)
                .map(coin -> ResponseEntity.ok(coin))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get all active tracked coins
     */
    @GetMapping("/active")
    public ResponseEntity<List<TrackedCoinDto>> getActiveTrackedCoins() {
        log.debug("Fetching all active tracked coins");
        
        List<TrackedCoinDto> activeCoins = coinTrackingService.getActiveTrackedCoins();
        return ResponseEntity.ok(activeCoins);
    }
    
    /**
     * Get tracked coins by exchange
     */
    @GetMapping("/exchange/{exchange}")
    public ResponseEntity<List<TrackedCoinDto>> getTrackedCoinsByExchange(@PathVariable TrackedCoin.Exchange exchange) {
        log.debug("Fetching tracked coins for exchange: {}", exchange);
        
        List<TrackedCoinDto> coins = coinTrackingService.getTrackedCoinsByExchange(exchange);
        return ResponseEntity.ok(coins);
    }
    
    /**
     * Get tracked coins with WebSocket enabled
     */
    @GetMapping("/websocket-enabled")
    public ResponseEntity<List<TrackedCoinDto>> getWebSocketEnabledCoins() {
        log.debug("Fetching WebSocket enabled tracked coins");
        
        List<TrackedCoinDto> coins = coinTrackingService.getWebSocketEnabledCoins();
        return ResponseEntity.ok(coins);
    }
    
    /**
     * Get tracked coins by quote currency
     */
    @GetMapping("/quote/{quoteCurrency}")
    public ResponseEntity<List<TrackedCoinDto>> getTrackedCoinsByQuoteCurrency(@PathVariable String quoteCurrency) {
        log.debug("Fetching tracked coins for quote currency: {}", quoteCurrency);
        
        List<TrackedCoinDto> coins = coinTrackingService.getTrackedCoinsByQuoteCurrency(quoteCurrency);
        return ResponseEntity.ok(coins);
    }
    
    /**
     * Get all tracked coins with pagination
     */
    @GetMapping
    public ResponseEntity<Page<TrackedCoinDto>> getAllTrackedCoins(Pageable pageable) {
        log.debug("Fetching tracked coins with pagination: {}", pageable);
        
        Page<TrackedCoinDto> coins = coinTrackingService.getAllTrackedCoins(pageable);
        return ResponseEntity.ok(coins);
    }
    
    /**
     * Update tracked coin
     */
    @PutMapping("/{id}")
    public ResponseEntity<TrackedCoinDto> updateTrackedCoin(@PathVariable Long id, @Valid @RequestBody TrackedCoinDto coinDto) {
        log.info("Updating tracked coin with ID: {}", id);
        
        try {
            TrackedCoinDto updatedCoin = coinTrackingService.updateTrackedCoin(id, coinDto);
            return ResponseEntity.ok(updatedCoin);
        } catch (IllegalArgumentException e) {
            log.error("Failed to update tracked coin: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Delete tracked coin
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrackedCoin(@PathVariable Long id) {
        log.info("Deleting tracked coin with ID: {}", id);
        
        try {
            coinTrackingService.deleteTrackedCoin(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Failed to delete tracked coin: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Activate tracked coin
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<TrackedCoinDto> activateTrackedCoin(@PathVariable Long id) {
        log.info("Activating tracked coin with ID: {}", id);
        
        try {
            TrackedCoinDto updatedCoin = coinTrackingService.toggleActivation(id, true);
            return ResponseEntity.ok(updatedCoin);
        } catch (IllegalArgumentException e) {
            log.error("Failed to activate tracked coin: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Deactivate tracked coin
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<TrackedCoinDto> deactivateTrackedCoin(@PathVariable Long id) {
        log.info("Deactivating tracked coin with ID: {}", id);
        
        try {
            TrackedCoinDto updatedCoin = coinTrackingService.toggleActivation(id, false);
            return ResponseEntity.ok(updatedCoin);
        } catch (IllegalArgumentException e) {
            log.error("Failed to deactivate tracked coin: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get coins with custom polling intervals
     */
    @GetMapping("/custom-polling")
    public ResponseEntity<List<TrackedCoinDto>> getCoinsWithCustomPolling() {
        log.debug("Fetching coins with custom polling intervals");
        
        List<TrackedCoinDto> coins = coinTrackingService.getCoinsWithCustomPolling();
        return ResponseEntity.ok(coins);
    }
    
    /**
     * Get coins by priority range
     */
    @GetMapping("/priority/{minPriority}")
    public ResponseEntity<List<TrackedCoinDto>> getCoinsByPriority(@PathVariable int minPriority) {
        log.debug("Fetching coins with priority >= {}", minPriority);
        
        List<TrackedCoinDto> coins = coinTrackingService.getCoinsByPriority(minPriority);
        return ResponseEntity.ok(coins);
    }
    
    /**
     * Get tracking statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<CoinTrackingService.TrackingStats> getTrackingStats() {
        log.debug("Fetching tracking statistics");
        
        CoinTrackingService.TrackingStats stats = coinTrackingService.getTrackingStats();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Bulk activate coins by symbols
     */
    @PatchMapping("/bulk-activate")
    public ResponseEntity<List<TrackedCoinDto>> bulkActivateCoins(@RequestBody Set<String> symbols) {
        log.info("Bulk activating {} coins", symbols.size());
        
        List<TrackedCoinDto> updatedCoins = coinTrackingService.bulkToggleActivation(symbols, true);
        return ResponseEntity.ok(updatedCoins);
    }
    
    /**
     * Bulk deactivate coins by symbols
     */
    @PatchMapping("/bulk-deactivate")
    public ResponseEntity<List<TrackedCoinDto>> bulkDeactivateCoins(@RequestBody Set<String> symbols) {
        log.info("Bulk deactivating {} coins", symbols.size());
        
        List<TrackedCoinDto> updatedCoins = coinTrackingService.bulkToggleActivation(symbols, false);
        return ResponseEntity.ok(updatedCoins);
    }
} 