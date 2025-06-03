package alg.coyote001.service.exchange;

import alg.coyote001.dto.TickerData;
import alg.coyote001.service.CacheService;
import alg.coyote001.service.RateLimitingService;
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
 * Binance Exchange Adapter
 * Implements Binance API v3 for cryptocurrency data
 */
@Component
@Slf4j
public class BinanceAdapter extends AbstractExchangeAdapter {
    
    private static final String BASE_URL = "https://api.binance.com";
    private static final String TICKER_ENDPOINT = "/api/v3/ticker/24hr";
    private static final String PING_ENDPOINT = "/api/v3/ping";
    private static final String EXCHANGE_INFO_ENDPOINT = "/api/v3/exchangeInfo";
    private static final int MAX_REQUESTS_PER_MINUTE = 1200;
    
    public BinanceAdapter(ObjectMapper objectMapper, 
                         CacheService cacheService, 
                         RateLimitingService rateLimitingService) {
        super(BASE_URL, MAX_REQUESTS_PER_MINUTE, objectMapper, cacheService, rateLimitingService);
        log.info("Initialized BinanceAdapter with base URL: {}", BASE_URL);
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
        log.info("Initializing Binance adapter");
        return isHealthy()
            .doOnNext(healthy -> {
                if (healthy) {
                    log.info("Binance adapter initialized successfully");
                } else {
                    log.warn("Binance adapter initialization failed - API not responding");
                }
            })
            .then();
    }
    
    @Override
    protected Mono<TickerData> fetchTickerFromExchange(String symbol) {
        // Symbol is already normalized by AbstractExchangeAdapter
        return executeRequest(
                webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path(TICKER_ENDPOINT)
                                .queryParam("symbol", symbol)
                                .build())
                        .retrieve()
                        .bodyToMono(JsonNode.class),
                JsonNode.class
        ).map(response -> parseTickerResponse(response, symbol))
         .onErrorReturn(createErrorTicker(symbol, "Failed to fetch ticker"));
    }
    
    @Override
    public Flux<TickerData> fetchTickers(List<String> symbols) {
        return Flux.fromIterable(symbols)
            .flatMap(this::fetchTicker)
            .doOnComplete(() -> log.debug("Fetched {} tickers from Binance", symbols.size()));
    }
    
    @Override
    public Mono<Void> subscribeToTicker(String symbol, Consumer<TickerData> callback) {
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
        // Clear any cache if needed
        if (cacheService != null) {
            cacheService.evictExchangeCache(getExchangeName());
        }
        return Mono.empty();
    }
    
    @Override
    public Mono<List<String>> getSupportedSymbols() {
        // Check cache first
        if (cacheService != null) {
            List<String> cached = cacheService.getCachedSymbols(getExchangeName());
            if (cached != null) {
                return Mono.just(cached);
            }
        }
        
        return webClient.get()
            .uri(EXCHANGE_INFO_ENDPOINT)
            .retrieve()
            .bodyToMono(String.class)
            .map(this::parseExchangeInfo)
            .doOnNext(symbols -> {
                if (cacheService != null) {
                    cacheService.cacheSymbols(getExchangeName(), symbols);
                }
            })
            .doOnError(error -> log.error("Failed to fetch exchange info from Binance", error))
            .onErrorReturn(List.of());
    }
    
    /**
     * Normalize symbol format for Binance
     * Example: BTC/USDT -> BTCUSDT
     */
    @Override
    protected String normalizeSymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
        
        // Remove any slashes, dashes, or dots and convert to uppercase
        return symbol.replaceAll("[/\\-\\.]", "").toUpperCase().trim();
    }
    
    /**
     * Parse Binance ticker response from JsonNode
     */
    private TickerData parseTickerResponse(JsonNode response, String originalSymbol) {
        try {
            // Validate required fields
            if (!response.has("symbol") || !response.has("lastPrice")) {
                log.error("Invalid Binance ticker response format: missing required fields");
                throw new RuntimeException("Invalid response format");
            }
            
            BigDecimal price = new BigDecimal(response.get("lastPrice").asText());
            BigDecimal volume24h = response.has("volume") ? new BigDecimal(response.get("volume").asText()) : BigDecimal.ZERO;
            BigDecimal change24h = response.has("priceChangePercent") ? new BigDecimal(response.get("priceChangePercent").asText()) : BigDecimal.ZERO;
            BigDecimal bid = response.has("bidPrice") ? new BigDecimal(response.get("bidPrice").asText()) : price;
            BigDecimal ask = response.has("askPrice") ? new BigDecimal(response.get("askPrice").asText()) : price;
            
            log.debug("Parsed Binance ticker - Symbol: {}, Price: {}, Volume: {}, Change: {}%", 
                originalSymbol, price, volume24h, change24h);
            
            return TickerData.builder()
                .exchange(getExchangeName())
                .symbol(originalSymbol)
                .price(price)
                .bid(bid)
                .ask(ask)
                .volume24h(volume24h)
                .change24h(change24h)
                .timestamp(LocalDateTime.now())
                .status(TickerData.TickerStatus.ACTIVE)
                .build();
                
        } catch (Exception e) {
            log.error("Failed to parse Binance ticker response: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse ticker response", e);
        }
    }
    
    /**
     * Parse exchange info to get supported symbols
     */
    private List<String> parseExchangeInfo(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode symbols = rootNode.get("symbols");
            
            if (symbols == null || !symbols.isArray()) {
                log.warn("Invalid exchange info response format");
                return List.of();
            }
            
            return symbols.findValuesAsText("symbol");
            
        } catch (Exception e) {
            log.error("Failed to parse Binance exchange info: {}", e.getMessage(), e);
            return List.of();
        }
    }
} 