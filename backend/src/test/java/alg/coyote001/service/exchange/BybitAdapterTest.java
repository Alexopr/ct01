package alg.coyote001.service.exchange;

import alg.coyote001.dto.TickerData;
import alg.coyote001.service.CacheService;
import alg.coyote001.service.RateLimitingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

/**
 * Unit тесты для BybitAdapter
 */
@ExtendWith(MockitoExtension.class)
class BybitAdapterTest {
    
    @Mock
    private CacheService cacheService;
    
    @Mock
    private RateLimitingService rateLimitingService;
    
    private BybitAdapter bybitAdapter;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        // Use real ObjectMapper instead of mock to avoid serialization issues
        objectMapper = new ObjectMapper();
        bybitAdapter = new BybitAdapter(objectMapper, cacheService, rateLimitingService);
        
        // Setup default mock behavior
        lenient().when(cacheService.getCachedTicker(anyString(), anyString())).thenReturn(null);
        lenient().when(cacheService.getCachedHealthCheck(anyString())).thenReturn(null);
        lenient().when(cacheService.isCacheHealthy()).thenReturn(true);
        lenient().when(rateLimitingService.isRequestAllowed(anyString(), any(Integer.class), any(Integer.class))).thenReturn(true);
        lenient().when(rateLimitingService.isInBackoff(anyString())).thenReturn(false);
    }
    
    @Test
    void testGetExchangeName() {
        // When
        String exchangeName = bybitAdapter.getExchangeName();
        
        // Then
        assertEquals("Bybit", exchangeName);
    }
    
    @Test
    void testNormalizeSymbol() {
        // Given
        String symbolWithSlash = "BTC/USDT";
        String symbolLowerCase = "eth/usdt";
        String symbolAlreadyNormalized = "SOLUSDT";
        
        // When (используем reflection для доступа к protected методу)
        String normalizedBTC = (String) ReflectionTestUtils.invokeMethod(
                bybitAdapter, "normalizeSymbol", symbolWithSlash);
        String normalizedETH = (String) ReflectionTestUtils.invokeMethod(
                bybitAdapter, "normalizeSymbol", symbolLowerCase);
        String normalizedSOL = (String) ReflectionTestUtils.invokeMethod(
                bybitAdapter, "normalizeSymbol", symbolAlreadyNormalized);
        
        // Then
        assertEquals("BTCUSDT", normalizedBTC);
        assertEquals("ETHUSDT", normalizedETH);
        assertEquals("SOLUSDT", normalizedSOL);
    }
    
    @Test
    void testRestoreSymbolFormat() {
        // When (используем reflection для доступа к private методу)
        String restoredBTC = (String) ReflectionTestUtils.invokeMethod(
                bybitAdapter, "restoreSymbolFormat", "BTCUSDT");
        String restoredETH = (String) ReflectionTestUtils.invokeMethod(
                bybitAdapter, "restoreSymbolFormat", "ETHUSDC");
        String restoredSOL = (String) ReflectionTestUtils.invokeMethod(
                bybitAdapter, "restoreSymbolFormat", "SOLBTC");
        String restoredLTC = (String) ReflectionTestUtils.invokeMethod(
                bybitAdapter, "restoreSymbolFormat", "LTCETH");
        String unknown = (String) ReflectionTestUtils.invokeMethod(
                bybitAdapter, "restoreSymbolFormat", "UNKNOWN");
        
        // Then
        assertEquals("BTC/USDT", restoredBTC);
        assertEquals("ETH/USDC", restoredETH);
        assertEquals("SOL/BTC", restoredSOL);
        assertEquals("LTC/ETH", restoredLTC);
        assertEquals("UNKNOWN", unknown);
    }
    
    @Test
    void testGetHealthCheckEndpoint() {
        // When
        String endpoint = (String) ReflectionTestUtils.invokeMethod(
                bybitAdapter, "getHealthCheckEndpoint");
        
        // Then
        assertEquals("/v5/market/time", endpoint);
    }
    
    @Test
    void testCreateErrorTicker() {
        // Given
        String symbol = "BTC/USDT";
        String error = "Network error";
        
        // When
        TickerData errorTicker = (TickerData) ReflectionTestUtils.invokeMethod(
                bybitAdapter, "createErrorTicker", symbol, error);
        
        // Then
        assertNotNull(errorTicker);
        assertEquals(symbol, errorTicker.getSymbol());
        assertEquals("Bybit", errorTicker.getExchange());
        assertEquals(BigDecimal.ZERO, errorTicker.getPrice());
        assertEquals(TickerData.TickerStatus.ERROR, errorTicker.getStatus());
        assertNotNull(errorTicker.getTimestamp());
    }
    
    @Test
    void testDisconnect() {
        // When
        var result = bybitAdapter.disconnect();
        
        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }
    
    @Test
    void testSubscribeToTicker_DoesNotThrowException() {
        // Given
        String symbol = "BTC/USDT";
        
        // When & Then - just verify the method can be called without throwing
        assertDoesNotThrow(() -> {
            bybitAdapter.subscribeToTicker(symbol, data -> {
                // Mock callback - do nothing
            });
        });
        
        // Note: In a real test environment, we would mock the WebSocket connection
        // For now, we just verify the method signature is correct
    }
} 