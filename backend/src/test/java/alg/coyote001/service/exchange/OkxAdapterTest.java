package alg.coyote001.service.exchange;

import alg.coyote001.dto.TickerData;
import alg.coyote001.service.CacheService;
import alg.coyote001.service.RateLimitingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

/**
 * Unit тесты для OkxAdapter
 */
@ExtendWith(MockitoExtension.class)
class OkxAdapterTest {
    
    @Mock
    private CacheService cacheService;
    
    @Mock
    private RateLimitingService rateLimitingService;
    
    private OkxAdapter okxAdapter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Use real ObjectMapper instead of mock to avoid serialization issues
        objectMapper = new ObjectMapper();
        okxAdapter = new OkxAdapter(objectMapper, cacheService, rateLimitingService);
        
        // Setup default mock behavior
        lenient().when(cacheService.getCachedTicker(anyString(), anyString())).thenReturn(null);
        lenient().when(cacheService.getCachedHealthCheck(anyString())).thenReturn(null);
        lenient().when(cacheService.getCachedSymbols(anyString())).thenReturn(null);
        lenient().when(cacheService.isCacheHealthy()).thenReturn(true);
        lenient().when(rateLimitingService.isRequestAllowed(anyString(), any(Integer.class), any(Integer.class))).thenReturn(true);
        lenient().when(rateLimitingService.isInBackoff(anyString())).thenReturn(false);
    }

    @Test
    void testGetExchangeName() {
        assertEquals("OKX", okxAdapter.getExchangeName());
    }

    @Test
    void testNormalizeSymbol() {
        // Test via reflection to access protected method
        String result1 = (String) ReflectionTestUtils.invokeMethod(okxAdapter, "normalizeSymbol", "BTC/USDT");
        String result2 = (String) ReflectionTestUtils.invokeMethod(okxAdapter, "normalizeSymbol", "ETH/USDT");
        String result3 = (String) ReflectionTestUtils.invokeMethod(okxAdapter, "normalizeSymbol", "ada/bnb");
        String result4 = (String) ReflectionTestUtils.invokeMethod(okxAdapter, "normalizeSymbol", "SOL-USDT");
        
        assertEquals("BTC-USDT", result1);
        assertEquals("ETH-USDT", result2);
        assertEquals("ADA-BNB", result3);
        assertEquals("SOL-USDT", result4);
    }

    @Test
    void testNormalizeSymbol_NullOrEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            ReflectionTestUtils.invokeMethod(okxAdapter, "normalizeSymbol", (Object) null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            ReflectionTestUtils.invokeMethod(okxAdapter, "normalizeSymbol", "");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            ReflectionTestUtils.invokeMethod(okxAdapter, "normalizeSymbol", "   ");
        });
    }

    @Test
    void testGetHealthCheckEndpoint() {
        String endpoint = (String) ReflectionTestUtils.invokeMethod(okxAdapter, "getHealthCheckEndpoint");
        assertEquals("/api/v5/system/status", endpoint);
    }

    @Test
    void testCreateErrorTicker() {
        String symbol = "BTC/USDT";
        String error = "Network error";
        
        TickerData errorTicker = (TickerData) ReflectionTestUtils.invokeMethod(
                okxAdapter, "createErrorTicker", symbol, error);
        
        assertNotNull(errorTicker);
        assertEquals(symbol, errorTicker.getSymbol());
        assertEquals("OKX", errorTicker.getExchange());
        assertEquals(BigDecimal.ZERO, errorTicker.getPrice());
        assertEquals(TickerData.TickerStatus.ERROR, errorTicker.getStatus());
        assertNotNull(errorTicker.getTimestamp());
    }

    @Test
    void testDisconnect() {
        var result = okxAdapter.disconnect();
        
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void testSubscribeAndUnsubscribe_DoNotThrow() {
        // These methods are placeholder implementations
        // Just verify they don't throw exceptions
        assertDoesNotThrow(() -> {
            okxAdapter.subscribeToTicker("BTC/USDT", ticker -> {});
        });
            
        assertDoesNotThrow(() -> {
            okxAdapter.unsubscribeFromTicker("BTC/USDT");
        });
    }
} 