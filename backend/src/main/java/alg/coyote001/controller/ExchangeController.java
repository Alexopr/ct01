package alg.coyote001.controller;

import alg.coyote001.dto.ExchangeDto;
import alg.coyote001.service.ExchangeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for exchange operations
 */
@RestController
@RequestMapping("/api/v1/exchanges")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ExchangeController {
    
    private final ExchangeService exchangeService;
    
    /**
     * Get all exchanges
     */
    @GetMapping
    public ResponseEntity<Page<ExchangeDto>> getAllExchanges(Pageable pageable) {
        log.debug("Fetching all exchanges with pagination: {}", pageable);
        
        Page<ExchangeDto> exchanges = exchangeService.getAllExchanges(pageable);
        return ResponseEntity.ok(exchanges);
    }
    
    /**
     * Get active exchanges only
     */
    @GetMapping("/active")
    public ResponseEntity<List<ExchangeDto>> getActiveExchanges() {
        log.debug("Fetching active exchanges");
        
        List<ExchangeDto> activeExchanges = exchangeService.getActiveExchanges();
        return ResponseEntity.ok(activeExchanges);
    }
    
    /**
     * Get exchange by name
     */
    @GetMapping("/{name}")
    public ResponseEntity<ExchangeDto> getExchangeByName(@PathVariable String name) {
        log.debug("Fetching exchange by name: {}", name);
        
        return exchangeService.getExchangeByName(name)
                .map(exchange -> ResponseEntity.ok(exchange))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get exchange status and health information
     */
    @GetMapping("/{name}/status")
    public ResponseEntity<Map<String, Object>> getExchangeStatus(@PathVariable String name) {
        log.info("Fetching status for exchange: {}", name);
        
        try {
            Map<String, Object> status = exchangeService.getExchangeStatus(name);
            return ResponseEntity.ok(status);
        } catch (IllegalArgumentException e) {
            log.error("Error fetching status for exchange {}: {}", name, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get supported trading pairs for an exchange
     */
    @GetMapping("/{name}/pairs")
    public ResponseEntity<List<String>> getSupportedPairs(@PathVariable String name) {
        log.debug("Fetching supported pairs for exchange: {}", name);
        
        try {
            List<String> pairs = exchangeService.getSupportedTradingPairs(name);
            return ResponseEntity.ok(pairs);
        } catch (IllegalArgumentException e) {
            log.error("Error fetching pairs for exchange {}: {}", name, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get exchange statistics
     */
    @GetMapping("/{name}/stats")
    public ResponseEntity<Map<String, Object>> getExchangeStats(@PathVariable String name) {
        log.debug("Fetching statistics for exchange: {}", name);
        
        try {
            Map<String, Object> stats = exchangeService.getExchangeStatistics(name);
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException e) {
            log.error("Error fetching statistics for exchange {}: {}", name, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get rate limit information for an exchange
     */
    @GetMapping("/{name}/rate-limits")
    public ResponseEntity<Map<String, Object>> getRateLimits(@PathVariable String name) {
        log.debug("Fetching rate limits for exchange: {}", name);
        
        try {
            Map<String, Object> rateLimits = exchangeService.getRateLimitInfo(name);
            return ResponseEntity.ok(rateLimits);
        } catch (IllegalArgumentException e) {
            log.error("Error fetching rate limits for exchange {}: {}", name, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
} 