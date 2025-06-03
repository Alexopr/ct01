package alg.coyote001.controller;

import alg.coyote001.dto.CoinDto;
import alg.coyote001.dto.PriceHistoryDto;
import alg.coyote001.entity.Exchange;
import alg.coyote001.service.CoinService;
import alg.coyote001.service.PriceHistoryService;
import alg.coyote001.service.ExchangeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for coin data operations
 */
@RestController
@RequestMapping("/api/v1/coins")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CoinDataController {
    
    private final CoinService coinService;
    private final PriceHistoryService priceHistoryService;
    private final ExchangeService exchangeService;
    
    /**
     * Get all tracked coins
     */
    @GetMapping
    public ResponseEntity<Page<CoinDto>> getAllCoins(Pageable pageable) {
        log.debug("Fetching all coins with pagination: {}", pageable);
        
        Page<CoinDto> coins = coinService.getAllCoins(pageable);
        return ResponseEntity.ok(coins);
    }
    
    /**
     * Get active coins only
     */
    @GetMapping("/active")
    public ResponseEntity<List<CoinDto>> getActiveCoins() {
        log.debug("Fetching active coins");
        
        List<CoinDto> activeCoins = coinService.getActiveCoins();
        return ResponseEntity.ok(activeCoins);
    }
    
    /**
     * Get coin by symbol
     */
    @GetMapping("/{symbol}")
    public ResponseEntity<CoinDto> getCoinBySymbol(@PathVariable String symbol) {
        log.debug("Fetching coin by symbol: {}", symbol);
        
        return coinService.getCoinBySymbol(symbol)
                .map(coin -> ResponseEntity.ok(coin))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get current price for a specific coin across all exchanges
     */
    @GetMapping("/{symbol}/price")
    public ResponseEntity<Map<String, Object>> getCurrentPrice(@PathVariable String symbol) {
        log.info("Fetching current price for coin: {}", symbol);
        
        try {
            Map<String, Object> priceData = priceHistoryService.getCurrentPriceData(symbol);
            return ResponseEntity.ok(priceData);
        } catch (IllegalArgumentException e) {
            log.error("Error fetching price for coin {}: {}", symbol, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get current price for a specific coin on a specific exchange
     */
    @GetMapping("/{symbol}/price/{exchangeName}")
    public ResponseEntity<Map<String, Object>> getCurrentPriceOnExchange(
            @PathVariable String symbol, 
            @PathVariable String exchangeName) {
        log.info("Fetching current price for coin: {} on exchange: {}", symbol, exchangeName);
        
        try {
            Map<String, Object> priceData = priceHistoryService.getCurrentPriceOnExchange(symbol, exchangeName);
            return ResponseEntity.ok(priceData);
        } catch (IllegalArgumentException e) {
            log.error("Error fetching price for coin {} on exchange {}: {}", symbol, exchangeName, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get historical price data for a coin
     */
    @GetMapping("/{symbol}/history")
    public ResponseEntity<Page<PriceHistoryDto>> getHistoricalData(
            @PathVariable String symbol,
            @RequestParam(required = false) String exchangeName,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            Pageable pageable) {
        
        log.info("Fetching historical data for coin: {} on exchange: {} from: {} to: {}", 
                symbol, exchangeName, from, to);
        
        try {
            Page<PriceHistoryDto> historicalData = priceHistoryService.getHistoricalData(
                    symbol, exchangeName, from, to, pageable);
            return ResponseEntity.ok(historicalData);
        } catch (IllegalArgumentException e) {
            log.error("Error fetching historical data: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get price statistics for a coin
     */
    @GetMapping("/{symbol}/stats")
    public ResponseEntity<Map<String, Object>> getPriceStatistics(
            @PathVariable String symbol,
            @RequestParam(required = false) String exchangeName,
            @RequestParam(defaultValue = "24") int hours) {
        
        log.info("Fetching price statistics for coin: {} on exchange: {} for {} hours", 
                symbol, exchangeName, hours);
        
        try {
            Map<String, Object> stats = priceHistoryService.getPriceStatistics(symbol, exchangeName, hours);
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException e) {
            log.error("Error fetching price statistics: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get coins by exchange
     */
    @GetMapping("/exchange/{exchangeName}")
    public ResponseEntity<List<CoinDto>> getCoinsByExchange(@PathVariable String exchangeName) {
        log.debug("Fetching coins for exchange: {}", exchangeName);
        
        try {
            List<CoinDto> coins = coinService.getCoinsByExchange(exchangeName);
            return ResponseEntity.ok(coins);
        } catch (IllegalArgumentException e) {
            log.error("Error fetching coins for exchange {}: {}", exchangeName, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Search coins by symbol pattern
     */
    @GetMapping("/search")
    public ResponseEntity<List<CoinDto>> searchCoins(@RequestParam String query) {
        log.debug("Searching coins with query: {}", query);
        
        List<CoinDto> coins = coinService.searchCoinsBySymbol(query);
        return ResponseEntity.ok(coins);
    }
} 