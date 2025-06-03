package alg.coyote001;

import alg.coyote001.service.exchange.BybitAdapter;
import alg.coyote001.service.exchange.BinanceAdapter;
import alg.coyote001.service.exchange.OkxAdapter;
import alg.coyote001.service.CacheService;
import alg.coyote001.service.RateLimitingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple test to verify compilation and basic functionality
 */
@ExtendWith(MockitoExtension.class)
class SimpleCompilationTest {
    
    @Mock
    private CacheService cacheService;
    
    @Mock
    private RateLimitingService rateLimitingService;
    
    @Test
    void testAdaptersCanBeInstantiated() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // Test that all adapters can be created with constructor injection
        BybitAdapter bybitAdapter = new BybitAdapter(objectMapper, cacheService, rateLimitingService);
        BinanceAdapter binanceAdapter = new BinanceAdapter(objectMapper, cacheService, rateLimitingService);
        OkxAdapter okxAdapter = new OkxAdapter(objectMapper, cacheService, rateLimitingService);
        
        assertNotNull(bybitAdapter);
        assertNotNull(binanceAdapter);
        assertNotNull(okxAdapter);
        
        assertEquals("Bybit", bybitAdapter.getExchangeName());
        assertEquals("BINANCE", binanceAdapter.getExchangeName());
        assertEquals("OKX", okxAdapter.getExchangeName());
    }
} 