package alg.coyote001.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Unit тесты для TickerData DTO
 */
class TickerDataTest {
    
    @Test
    void testTickerDataBuilder() {
        // Given
        String symbol = "BTC/USDT";
        String exchange = "Bybit";
        BigDecimal price = new BigDecimal("50000.00");
        BigDecimal bid = new BigDecimal("49999.50");
        BigDecimal ask = new BigDecimal("50000.50");
        BigDecimal volume24h = new BigDecimal("1000.5");
        BigDecimal change24h = new BigDecimal("2.5");
        LocalDateTime timestamp = LocalDateTime.now();
        TickerData.TickerStatus status = TickerData.TickerStatus.ACTIVE;
        
        // When
        TickerData tickerData = TickerData.builder()
                .symbol(symbol)
                .exchange(exchange)
                .price(price)
                .bid(bid)
                .ask(ask)
                .volume24h(volume24h)
                .change24h(change24h)
                .timestamp(timestamp)
                .status(status)
                .build();
        
        // Then
        assertEquals(symbol, tickerData.getSymbol());
        assertEquals(exchange, tickerData.getExchange());
        assertEquals(price, tickerData.getPrice());
        assertEquals(bid, tickerData.getBid());
        assertEquals(ask, tickerData.getAsk());
        assertEquals(volume24h, tickerData.getVolume24h());
        assertEquals(change24h, tickerData.getChange24h());
        assertEquals(timestamp, tickerData.getTimestamp());
        assertEquals(status, tickerData.getStatus());
    }
    
    @Test
    void testTickerDataDefaultConstructor() {
        // When
        TickerData tickerData = new TickerData();
        
        // Then
        assertNull(tickerData.getSymbol());
        assertNull(tickerData.getExchange());
        assertNull(tickerData.getPrice());
        assertNull(tickerData.getBid());
        assertNull(tickerData.getAsk());
        assertNull(tickerData.getVolume24h());
        assertNull(tickerData.getChange24h());
        assertNull(tickerData.getTimestamp());
        assertNull(tickerData.getStatus());
        assertNull(tickerData.getError());
    }
    
    @Test
    void testTickerDataAllArgsConstructor() {
        // Given
        String symbol = "ETH/USDT";
        String exchange = "Binance";
        BigDecimal price = new BigDecimal("3000.00");
        BigDecimal bid = new BigDecimal("2999.50");
        BigDecimal ask = new BigDecimal("3000.50");
        BigDecimal volume24h = new BigDecimal("500.0");
        BigDecimal change24h = new BigDecimal("-1.2");
        LocalDateTime timestamp = LocalDateTime.now();
        TickerData.TickerStatus status = TickerData.TickerStatus.STALE;
        
        // When
        TickerData tickerData = new TickerData(symbol, exchange, price, bid, ask, 
                volume24h, change24h, timestamp, status, null);
        
        // Then
        assertEquals(symbol, tickerData.getSymbol());
        assertEquals(exchange, tickerData.getExchange());
        assertEquals(price, tickerData.getPrice());
        assertEquals(bid, tickerData.getBid());
        assertEquals(ask, tickerData.getAsk());
        assertEquals(volume24h, tickerData.getVolume24h());
        assertEquals(change24h, tickerData.getChange24h());
        assertEquals(timestamp, tickerData.getTimestamp());
        assertEquals(status, tickerData.getStatus());
        assertNull(tickerData.getError());
    }
    
    @Test
    void testTickerStatusEnum() {
        // Test all enum values
        assertEquals(3, TickerData.TickerStatus.values().length);
        
        assertTrue(TickerData.TickerStatus.valueOf("ACTIVE") == TickerData.TickerStatus.ACTIVE);
        assertTrue(TickerData.TickerStatus.valueOf("STALE") == TickerData.TickerStatus.STALE);
        assertTrue(TickerData.TickerStatus.valueOf("ERROR") == TickerData.TickerStatus.ERROR);
    }
    
    @Test
    void testEqualsAndHashCode() {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();
        
        TickerData ticker1 = TickerData.builder()
                .symbol("BTC/USDT")
                .exchange("Bybit")
                .price(new BigDecimal("50000"))
                .timestamp(timestamp)
                .status(TickerData.TickerStatus.ACTIVE)
                .build();
        
        TickerData ticker2 = TickerData.builder()
                .symbol("BTC/USDT")
                .exchange("Bybit")
                .price(new BigDecimal("50000"))
                .timestamp(timestamp)
                .status(TickerData.TickerStatus.ACTIVE)
                .build();
        
        TickerData ticker3 = TickerData.builder()
                .symbol("ETH/USDT")
                .exchange("Bybit")
                .price(new BigDecimal("3000"))
                .timestamp(timestamp)
                .status(TickerData.TickerStatus.ACTIVE)
                .build();
        
        // Then
        assertEquals(ticker1, ticker2);
        assertEquals(ticker1.hashCode(), ticker2.hashCode());
        assertNotEquals(ticker1, ticker3);
        assertNotEquals(ticker1.hashCode(), ticker3.hashCode());
    }
    
    @Test
    void testToString() {
        // Given
        TickerData ticker = TickerData.builder()
                .symbol("SOL/USDT")
                .exchange("OKX")
                .price(new BigDecimal("100.50"))
                .status(TickerData.TickerStatus.ACTIVE)
                .build();
        
        // When
        String result = ticker.toString();
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("SOL/USDT"));
        assertTrue(result.contains("OKX"));
        assertTrue(result.contains("100.50"));
        assertTrue(result.contains("ACTIVE"));
    }
    
    @Test
    void testTickerDataWithError() {
        // Given
        String symbol = "INVALID/PAIR";
        String exchange = "TestExchange";
        String errorMessage = "Invalid trading pair";
        TickerData.TickerStatus status = TickerData.TickerStatus.ERROR;
        
        // When
        TickerData tickerData = TickerData.builder()
                .symbol(symbol)
                .exchange(exchange)
                .status(status)
                .error(errorMessage)
                .build();
        
        // Then
        assertEquals(symbol, tickerData.getSymbol());
        assertEquals(exchange, tickerData.getExchange());
        assertEquals(status, tickerData.getStatus());
        assertEquals(errorMessage, tickerData.getError());
        assertNull(tickerData.getPrice());
    }
} 