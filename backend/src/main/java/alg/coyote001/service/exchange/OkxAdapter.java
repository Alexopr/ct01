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
 * OKX Exchange Adapter
 * Implements OKX API v5 for cryptocurrency data
 */
@Component
@Slf4j
public class OkxAdapter extends AbstractExchangeAdapter {
    
    private static final String BASE_URL = "https://www.okx.com";
    private static final String TICKER_ENDPOINT = "/api/v5/market/ticker";
    private static final String TICKERS_ENDPOINT = "/api/v5/market/tickers";
    private static final String STATUS_ENDPOINT = "/api/v5/system/status";
    private static final String INSTRUMENTS_ENDPOINT = "/api/v5/public/instruments";
    private static final int MAX_REQUESTS_PER_MINUTE = 600; // 20 requests per 2 seconds = ~600 per minute
    
    public OkxAdapter(ObjectMapper objectMapper, 
                     CacheService cacheService, 
                     RateLimitingService rateLimitingService) {
        super(BASE_URL, MAX_REQUESTS_PER_MINUTE, objectMapper, cacheService, rateLimitingService);
        log.info("Initialized OkxAdapter with base URL: {}", BASE_URL);
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
        log.info("Initializing OKX adapter");
        return isHealthy()
            .doOnNext(healthy -> {
                if (healthy) {
                    log.info("OKX adapter initialized successfully");
                } else {
                    log.warn("OKX adapter initialization failed - API not responding");
                }
            })
            .then();
    }
    
    @Override
    protected Mono<TickerData> fetchTickerFromExchange(String symbol) {
        // Symbol is already normalized by AbstractExchangeAdapter  
        log.debug("Fetching ticker for normalized symbol: {}", symbol);
        
        return executeRequest(
                webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path(TICKER_ENDPOINT)
                                .queryParam("instId", symbol)
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
            .doOnComplete(() -> log.debug("Fetched {} tickers from OKX", symbols.size()));
    }
    
    @Override
    public Mono<Void> subscribeToTicker(String symbol, Consumer<TickerData> callback) {
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
            .uri(uriBuilder -> uriBuilder
                .path(INSTRUMENTS_ENDPOINT)
                .queryParam("instType", "SPOT")
                .build())
            .retrieve()
            .bodyToMono(String.class)
            .map(this::parseInstruments)
            .doOnNext(symbols -> {
                if (cacheService != null) {
                    cacheService.cacheSymbols(getExchangeName(), symbols);
                }
            })
            .doOnError(error -> log.error("Failed to fetch instruments from OKX", error))
            .onErrorReturn(List.of());
    }
    
    /**
     * Normalize symbol format for OKX
     * Example: BTC/USDT -> BTC-USDT
     */
    @Override
    protected String normalizeSymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
        
        // Convert slashes to dashes and convert to uppercase
        return symbol.replace("/", "-")
                    .replace(".", "-")
                    .toUpperCase()
                    .trim();
    }
    
    /**
     * Parse OKX ticker response from JsonNode
     */
    private TickerData parseTickerResponse(JsonNode response, String originalSymbol) {
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
            BigDecimal volume24h = tickerData.has("vol24h") ? new BigDecimal(tickerData.get("vol24h").asText()) : BigDecimal.ZERO;
            BigDecimal bid = tickerData.has("bidPx") ? new BigDecimal(tickerData.get("bidPx").asText()) : price;
            BigDecimal ask = tickerData.has("askPx") ? new BigDecimal(tickerData.get("askPx").asText()) : price;
            
            // Calculate price change percentage
            BigDecimal change24h = BigDecimal.ZERO;
            if (tickerData.has("open24h")) {
                BigDecimal open24h = new BigDecimal(tickerData.get("open24h").asText());
                if (open24h.compareTo(BigDecimal.ZERO) != 0) {
                    change24h = price.subtract(open24h)
                        .divide(open24h, 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(new BigDecimal("100"));
                }
            }
            
            log.debug("Parsed OKX ticker - Symbol: {}, Price: {}, Volume: {}, Change: {}%", 
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
            log.error("Failed to parse OKX ticker response: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse ticker response", e);
        }
    }
    
    /**
     * Parse instruments to get supported symbols
     */
    private List<String> parseInstruments(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            
            if (!rootNode.has("code") || !"0".equals(rootNode.get("code").asText())) {
                String errorMsg = rootNode.has("msg") ? rootNode.get("msg").asText() : "Unknown error";
                log.error("OKX instruments API error: {}", errorMsg);
                return List.of();
            }
            
            JsonNode dataArray = rootNode.get("data");
            if (dataArray == null || !dataArray.isArray()) {
                log.warn("Invalid OKX instruments response format");
                return List.of();
            }
            
            return dataArray.findValuesAsText("instId");
            
        } catch (Exception e) {
            log.error("Failed to parse OKX instruments: {}", e.getMessage(), e);
            return List.of();
        }
    }
} 