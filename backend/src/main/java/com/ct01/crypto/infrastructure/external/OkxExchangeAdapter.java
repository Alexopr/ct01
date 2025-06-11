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
 * OKX Exchange Adapter для DDD Infrastructure
 * Реализует OKX API v5 для получения данных о криптовалютах
 */
@Component("dddOkxAdapter")
@Slf4j
public class OkxExchangeAdapter extends AbstractExchangeAdapter {
    
    private static final String BASE_URL = "https://www.okx.com";
    private static final String TICKER_ENDPOINT = "/api/v5/market/ticker";
    private static final String TICKERS_ENDPOINT = "/api/v5/market/tickers";
    private static final String STATUS_ENDPOINT = "/api/v5/system/status";
    private static final String INSTRUMENTS_ENDPOINT = "/api/v5/public/instruments";
    private static final int MAX_REQUESTS_PER_MINUTE = 600; // 20 requests per 2 seconds = ~600 per minute
    
    public OkxExchangeAdapter(ObjectMapper objectMapper) {
        super(BASE_URL, MAX_REQUESTS_PER_MINUTE, objectMapper);
        log.info("Initialized DDD OkxExchangeAdapter");
    }
    
    @Override
    public String getExchangeName() {
        return "OKX";
    }
    
    @Override
    protected String getHealthCheckEndpoint() {
        return STATUS_ENDPOINT;
    }
    
    @Override
    public Mono<Void> initialize() {
        log.info("Initializing OKX DDD adapter");
        return isHealthy()
                .doOnNext(healthy -> {
                    if (healthy) {
                        log.info("OKX DDD adapter initialized successfully");
                    } else {
                        log.warn("OKX DDD adapter initialization failed - API not responding");
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
                                .queryParam("instId", normalizedSymbol)
                                .build())
                        .retrieve()
                        .bodyToMono(JsonNode.class),
                JsonNode.class
        ).map(response -> parseTickerResponse(response, symbol))
         .onErrorReturn(createErrorPriceHistory(symbol, "Failed to fetch ticker from OKX"));
    }
    
    @Override
    public Flux<PriceHistory> fetchTickers(List<String> symbols) {
        return Flux.fromIterable(symbols)
                .flatMap(this::fetchTicker)
                .doOnComplete(() -> log.debug("Fetched {} tickers from OKX", symbols.size()));
    }
    
    @Override
    public Mono<Void> subscribeToTicker(String symbol, Consumer<PriceHistory> callback) {
        // WebSocket subscription implementation would go here
        log.warn("WebSocket subscription not implemented yet for OKX symbol: {}", symbol);
        return Mono.empty();
    }
    
    @Override
    public Mono<Void> unsubscribeFromTicker(String symbol) {
        // WebSocket unsubscription implementation would go here
        log.warn("WebSocket unsubscription not implemented yet for OKX symbol: {}", symbol);
        return Mono.empty();
    }
    
    @Override
    public Mono<Void> disconnect() {
        log.info("Disconnecting from OKX");
        return Mono.empty();
    }
    
    @Override
    public Mono<List<String>> getSupportedSymbols() {
        return executeRequest(
                webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path(INSTRUMENTS_ENDPOINT)
                                .queryParam("instType", "SPOT")
                                .build())
                        .retrieve()
                        .bodyToMono(JsonNode.class),
                JsonNode.class
        ).map(this::parseSymbolsResponse)
         .doOnError(error -> log.error("Failed to fetch symbols from OKX", error))
         .onErrorReturn(List.of());
    }
    
    @Override
    protected String normalizeSymbol(String symbol) {
        // OKX format: BTC-USDT (dash separator)
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
        
        String normalized = symbol.toUpperCase();
        
        // Convert standard format to OKX format
        if (normalized.contains("/")) {
            normalized = normalized.replace("/", "-");
        } else if (!normalized.contains("-") && !normalized.contains("USDT")) {
            // Add USDT if not present
            normalized += "-USDT";
        } else if (!normalized.contains("-")) {
            // Insert dash before USDT
            normalized = normalized.replace("USDT", "-USDT");
        }
        
        log.debug("Normalized symbol {} to {}", symbol, normalized);
        return normalized;
    }
    
    @Override
    protected String restoreSymbolFormat(String normalizedSymbol) {
        return normalizedSymbol.replace("-", "/");
    }
    
    /**
     * Парсинг ответа тикера от OKX
     */
    private PriceHistory parseTickerResponse(JsonNode response, String originalSymbol) {
        try {
            // OKX API returns response in format: {"code":"0","msg":"","data":[{...}]}
            if (!response.has("code") || !"0".equals(response.get("code").asText())) {
                String errorMsg = response.has("msg") ? response.get("msg").asText() : "Unknown error";
                log.error("OKX API error: {}", errorMsg);
                throw new RuntimeException("OKX API error: " + errorMsg);
            }
            
            JsonNode dataArray = response.get("data");
            if (dataArray == null || !dataArray.isArray() || dataArray.size() == 0) {
                log.error("Invalid OKX ticker response format: missing or empty data array");
                throw new RuntimeException("Invalid response format");
            }
            
            JsonNode tickerData = dataArray.get(0);
            
            // Validate required fields
            if (!tickerData.has("instId") || !tickerData.has("last")) {
                log.error("Invalid OKX ticker response format: missing required fields");
                throw new RuntimeException("Invalid response format");
            }
            
            BigDecimal price = new BigDecimal(tickerData.get("last").asText());
            BigDecimal volume24h = tickerData.has("vol24h") ? 
                    new BigDecimal(tickerData.get("vol24h").asText()) : BigDecimal.ZERO;
            BigDecimal change24h = tickerData.has("sodUtc0") ? 
                    new BigDecimal(tickerData.get("sodUtc0").asText()) : BigDecimal.ZERO;
            
            return createPriceHistory(
                    originalSymbol,
                    getExchangeName(),
                    price,
                    volume24h,
                    change24h,
                    LocalDateTime.now()
            );
            
        } catch (Exception e) {
            log.error("Error parsing OKX ticker response for {}: {}", originalSymbol, e.getMessage());
            throw new RuntimeException("Failed to parse ticker response", e);
        }
    }
    
    /**
     * Парсинг списка символов от OKX
     */
    private List<String> parseSymbolsResponse(JsonNode response) {
        try {
            if (!response.has("code") || !"0".equals(response.get("code").asText())) {
                String errorMsg = response.has("msg") ? response.get("msg").asText() : "Unknown error";
                log.error("OKX API error: {}", errorMsg);
                return List.of();
            }
            
            JsonNode dataArray = response.get("data");
            if (dataArray == null || !dataArray.isArray()) {
                log.error("Invalid OKX instruments response format");
                return List.of();
            }
            
            return java.util.stream.StreamSupport.stream(dataArray.spliterator(), false)
                    .filter(instrument -> "live".equals(instrument.get("state").asText()))
                    .map(instrument -> instrument.get("instId").asText())
                    .filter(symbol -> symbol.endsWith("-USDT")) // Only USDT pairs
                    .map(symbol -> symbol.replace("-", "/")) // Convert to standard format
                    .sorted()
                    .toList();
                    
        } catch (Exception e) {
            log.error("Error parsing OKX symbols response: {}", e.getMessage());
            return List.of();
        }
    }
}
