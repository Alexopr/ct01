package com.ct01.crypto.infrastructure.external;

import com.ct01.crypto.domain.PriceHistory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

/**
 * Binance Exchange Adapter для DDD Infrastructure
 * Реализует Binance API v3 для получения данных о криптовалютах
 */
@Component("dddBinanceAdapter")
@Slf4j
public class BinanceExchangeAdapter extends AbstractExchangeAdapter {
    
    private static final String BASE_URL = "https://api.binance.com";
    private static final String TICKER_ENDPOINT = "/api/v3/ticker/24hr";
    private static final String PING_ENDPOINT = "/api/v3/ping";
    private static final String EXCHANGE_INFO_ENDPOINT = "/api/v3/exchangeInfo";
    private static final int MAX_REQUESTS_PER_MINUTE = 1200;
    
    public BinanceExchangeAdapter(ObjectMapper objectMapper) {
        super(BASE_URL, MAX_REQUESTS_PER_MINUTE, objectMapper);
        log.info("Initialized DDD BinanceExchangeAdapter");
    }
    
    @Override
    public String getExchangeName() {
        return "BINANCE";
    }
    
    @Override
    protected String getHealthCheckEndpoint() {
        return PING_ENDPOINT;
    }
    
    @Override
    public Mono<Void> initialize() {
        log.info("Initializing Binance DDD adapter");
        return isHealthy()
                .doOnNext(healthy -> {
                    if (healthy) {
                        log.info("Binance DDD adapter initialized successfully");
                    } else {
                        log.warn("Binance DDD adapter initialization failed - API not responding");
                    }
                })
                .then();
    }
    
    @Override
    public Mono<PriceHistory> fetchTicker(String symbol) {
        String normalizedSymbol = normalizeSymbol(symbol);
        log.debug("Fetching ticker for normalized symbol: {}", normalizedSymbol);
        
        return executeRequest(
                webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path(TICKER_ENDPOINT)
                                .queryParam("symbol", normalizedSymbol)
                                .build())
                        .retrieve()
                        .bodyToMono(JsonNode.class),
                JsonNode.class
        ).map(response -> parseTickerResponse(response, symbol))
         .onErrorReturn(createErrorPriceHistory(symbol, "Failed to fetch ticker from Binance"));
    }
    
    @Override
    public Flux<PriceHistory> fetchTickers(List<String> symbols) {
        return Flux.fromIterable(symbols)
                .flatMap(this::fetchTicker)
                .doOnComplete(() -> log.debug("Fetched {} tickers from Binance", symbols.size()));
    }
    
    @Override
    public Mono<Void> subscribeToTicker(String symbol, Consumer<PriceHistory> callback) {
        // WebSocket subscription implementation would go here
        log.warn("WebSocket subscription not implemented yet for Binance symbol: {}", symbol);
        return Mono.empty();
    }
    
    @Override
    public Mono<Void> unsubscribeFromTicker(String symbol) {
        // WebSocket unsubscription implementation would go here
        log.warn("WebSocket unsubscription not implemented yet for Binance symbol: {}", symbol);
        return Mono.empty();
    }
    
    @Override
    public Mono<Void> disconnect() {
        log.info("Disconnecting from Binance");
        return Mono.empty();
    }
    
    @Override
    public Mono<List<String>> getSupportedSymbols() {
        return executeRequest(
                webClient.get()
                        .uri(EXCHANGE_INFO_ENDPOINT)
                        .retrieve()
                        .bodyToMono(JsonNode.class),
                JsonNode.class
        ).map(this::parseSymbolsResponse)
         .doOnError(error -> log.error("Failed to fetch symbols from Binance", error))
         .onErrorReturn(List.of());
    }
    
    @Override
    protected String normalizeSymbol(String symbol) {
        // Binance format: BTCUSDT (no separator)
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
        
        String normalized = symbol.toUpperCase();
        
        // Remove common separators
        normalized = normalized.replace("/", "").replace("-", "").replace("_", "");
        
        // Add USDT if not present and not already a complete pair
        if (!normalized.contains("USDT") && !normalized.contains("BUSD") && 
            !normalized.contains("BTC") && !normalized.contains("ETH")) {
            normalized += "USDT";
        }
        
        log.debug("Normalized symbol {} to {}", symbol, normalized);
        return normalized;
    }
    
    /**
     * Парсинг ответа тикера от Binance
     */
    private PriceHistory parseTickerResponse(JsonNode response, String originalSymbol) {
        try {
            if (!response.has("symbol") || !response.has("lastPrice")) {
                log.error("Invalid Binance ticker response format: missing required fields");
                throw new RuntimeException("Invalid response format");
            }
            
            String symbol = response.get("symbol").asText();
            BigDecimal price = new BigDecimal(response.get("lastPrice").asText());
            BigDecimal volume24h = response.has("volume") ? 
                    new BigDecimal(response.get("volume").asText()) : BigDecimal.ZERO;
            BigDecimal change24h = response.has("priceChangePercent") ? 
                    new BigDecimal(response.get("priceChangePercent").asText()) : BigDecimal.ZERO;
            
            return createPriceHistory(
                    originalSymbol,
                    getExchangeName(),
                    price,
                    volume24h,
                    change24h,
                    LocalDateTime.now()
            );
            
        } catch (Exception e) {
            log.error("Error parsing Binance ticker response for {}: {}", originalSymbol, e.getMessage());
            throw new RuntimeException("Failed to parse ticker response", e);
        }
    }
    
    /**
     * Парсинг списка символов от Binance
     */
    private List<String> parseSymbolsResponse(JsonNode response) {
        try {
            if (!response.has("symbols")) {
                log.error("Invalid Binance exchange info response: missing symbols");
                return List.of();
            }
            
            JsonNode symbolsArray = response.get("symbols");
            return java.util.stream.StreamSupport.stream(symbolsArray.spliterator(), false)
                    .filter(symbolNode -> "TRADING".equals(symbolNode.get("status").asText()))
                    .map(symbolNode -> symbolNode.get("symbol").asText())
                    .filter(symbol -> symbol.endsWith("USDT")) // Only USDT pairs
                    .map(symbol -> symbol.replace("USDT", "/USDT")) // Convert to standard format
                    .sorted()
                    .toList();
                    
        } catch (Exception e) {
            log.error("Error parsing Binance symbols response: {}", e.getMessage());
            return List.of();
        }
    }
} 
