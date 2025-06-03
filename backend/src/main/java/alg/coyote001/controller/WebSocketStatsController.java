package alg.coyote001.controller;

import alg.coyote001.websocket.PriceWebSocketController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for WebSocket connection statistics and management
 */
@RestController
@RequestMapping("/api/v1/websocket")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class WebSocketStatsController {
    
    private final PriceWebSocketController priceWebSocketController;
    
    /**
     * Get WebSocket connection statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getConnectionStats() {
        log.debug("Fetching WebSocket connection statistics");
        
        Map<String, Object> stats = priceWebSocketController.getConnectionStats();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Broadcast test message to all connected clients
     */
    @PostMapping("/broadcast-test")
    public ResponseEntity<Map<String, String>> broadcastTestMessage(@RequestParam String symbol) {
        log.info("Broadcasting test message for symbol: {}", symbol);
        
        Map<String, Object> testData = Map.of(
            "price", 50000.0,
            "volume", 1234.56,
            "change24h", 2.5,
            "exchange", "TEST",
            "timestamp", System.currentTimeMillis()
        );
        
        priceWebSocketController.broadcastPriceUpdate(symbol, testData);
        
        return ResponseEntity.ok(Map.of(
            "message", "Test message broadcasted",
            "symbol", symbol
        ));
    }
} 