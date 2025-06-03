package alg.coyote001.service.exchange;

import alg.coyote001.dto.TickerData;
import alg.coyote001.service.CacheService;
import alg.coyote001.service.RateLimitingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Адаптер для биржи Bybit
 * Документация API: https://bybit-exchange.github.io/docs/v5/
 * WebSocket API: https://bybit-exchange.github.io/docs/v5/websocket/public/ticker
 */
@Slf4j
@Component
public class BybitAdapter extends AbstractExchangeAdapter {
    
    private static final String BYBIT_BASE_URL = "https://api.bybit.com";
    private static final String BYBIT_WS_URL = "wss://stream.bybit.com/v5/public/spot";
    private static final int BYBIT_RATE_LIMIT = 120; // requests per minute
    
    // WebSocket management
    private final WebSocketClient webSocketClient;
    private final Map<String, Consumer<TickerData>> subscriptions = new ConcurrentHashMap<>();
    private final Sinks.Many<String> commandSink = Sinks.many().multicast().onBackpressureBuffer();
    private WebSocketSession currentSession;
    private volatile boolean isConnected = false;
    
    public BybitAdapter(ObjectMapper objectMapper, 
                       CacheService cacheService, 
                       RateLimitingService rateLimitingService) {
        super(BYBIT_BASE_URL, BYBIT_RATE_LIMIT, objectMapper, cacheService, rateLimitingService);
        this.webSocketClient = new ReactorNettyWebSocketClient();
    }
    
    @Override
    public String getExchangeName() {
        return "Bybit";
    }
    
    @Override
    public Mono<Void> initialize() {
        log.info("Initializing Bybit adapter");
        return isHealthy()
                .doOnNext(healthy -> {
                    if (healthy) {
                        log.info("Bybit adapter initialized successfully");
                    } else {
                        log.error("Failed to initialize Bybit adapter - health check failed");
                    }
                })
                .then();
    }
    
    @Override
    protected Mono<TickerData> fetchTickerFromExchange(String symbol) {
        // This method is called by AbstractExchangeAdapter.fetchTicker()
        // Symbol is already normalized here
        return executeRequest(
                webClient.get()
                        .uri("/v5/market/tickers", uriBuilder -> uriBuilder
                                .queryParam("category", "spot")
                                .queryParam("symbol", symbol)
                                .build())
                        .retrieve()
                        .bodyToMono(JsonNode.class),
                JsonNode.class
        ).map(this::parseTickerResponse)
         .onErrorReturn(createErrorTicker(symbol, "Failed to fetch ticker"));
    }
    
    @Override
    public Flux<TickerData> fetchTickers(List<String> symbols) {
        return Flux.fromIterable(symbols)
                .flatMap(this::fetchTicker, 3) // Параллельно до 3 запросов
                .onErrorContinue((error, symbol) -> 
                        log.error("Failed to fetch ticker for {}: {}", symbol, error.getMessage()));
    }
    
    @Override
    public Mono<Void> subscribeToTicker(String symbol, Consumer<TickerData> callback) {
        String normalizedSymbol = normalizeSymbol(symbol);
        subscriptions.put(normalizedSymbol, callback);
        
        return ensureWebSocketConnection()
                .then(sendSubscribeCommand(normalizedSymbol))
                .doOnSuccess(v -> log.info("Successfully subscribed to ticker updates for {}", symbol))
                .doOnError(error -> {
                    log.error("Failed to subscribe to ticker for {}: {}", symbol, error.getMessage());
                    subscriptions.remove(normalizedSymbol);
                });
    }
    
    @Override
    public Mono<Void> unsubscribeFromTicker(String symbol) {
        String normalizedSymbol = normalizeSymbol(symbol);
        subscriptions.remove(normalizedSymbol);
        
        if (currentSession != null && isConnected) {
            return sendUnsubscribeCommand(normalizedSymbol)
                    .doOnSuccess(v -> log.info("Successfully unsubscribed from ticker updates for {}", symbol))
                    .doOnError(error -> log.error("Failed to unsubscribe from ticker for {}: {}", symbol, error.getMessage()));
        }
        
        return Mono.empty();
    }
    
    @Override
    public Mono<Void> disconnect() {
        log.info("Disconnecting from Bybit");
        isConnected = false;
        subscriptions.clear();
        
        if (currentSession != null) {
            return currentSession.close()
                    .doFinally(signal -> {
                        currentSession = null;
                        // Clear any cache if needed
                        if (cacheService != null) {
                            cacheService.evictExchangeCache(getExchangeName());
                        }
                    });
        }
        
        return Mono.empty();
    }
    
    /**
     * Ensure WebSocket connection is established
     */
    private Mono<Void> ensureWebSocketConnection() {
        if (isConnected && currentSession != null) {
            return Mono.empty();
        }
        
        return connectWebSocket()
                .doOnSuccess(v -> log.info("WebSocket connection established"))
                .doOnError(error -> log.error("Failed to establish WebSocket connection: {}", error.getMessage()));
    }
    
    /**
     * Connect to Bybit WebSocket
     */
    private Mono<Void> connectWebSocket() {
        URI uri = URI.create(BYBIT_WS_URL);
        
        return webSocketClient.execute(uri, session -> {
            currentSession = session;
            isConnected = true;
            
            // Handle outgoing messages (subscriptions/unsubscriptions)
            Flux<WebSocketMessage> outgoing = commandSink.asFlux()
                    .map(session::textMessage);
            
            // Handle incoming messages
            Mono<Void> incoming = session.receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(this::handleWebSocketMessage)
                    .doOnError(error -> {
                        log.error("WebSocket error: {}", error.getMessage());
                        isConnected = false;
                    })
                    .then();
            
            // Send outgoing and process incoming
            return session.send(outgoing)
                    .and(incoming)
                    .doFinally(signal -> {
                        isConnected = false;
                        currentSession = null;
                        log.info("WebSocket connection closed");
                    });
        })
        .timeout(Duration.ofSeconds(30))
        .retry(3);
    }
    
    /**
     * Send subscribe command for a symbol
     */
    private Mono<Void> sendSubscribeCommand(String symbol) {
        String command = String.format("{\"op\":\"subscribe\",\"args\":[\"tickers.%s\"]}", symbol);
        commandSink.tryEmitNext(command);
        return Mono.empty();
    }
    
    /**
     * Send unsubscribe command for a symbol
     */
    private Mono<Void> sendUnsubscribeCommand(String symbol) {
        String command = String.format("{\"op\":\"unsubscribe\",\"args\":[\"tickers.%s\"]}", symbol);
        commandSink.tryEmitNext(command);
        return Mono.empty();
    }
    
    /**
     * Handle incoming WebSocket messages
     */
    private void handleWebSocketMessage(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            
            // Handle subscription confirmations
            if (jsonNode.has("success") && jsonNode.get("success").asBoolean()) {
                log.debug("Subscription confirmed: {}", message);
                return;
            }
            
            // Handle ticker data
            if (jsonNode.has("topic") && jsonNode.get("topic").asText().startsWith("tickers.")) {
                handleTickerUpdate(jsonNode);
            }
            
        } catch (Exception e) {
            log.error("Failed to parse WebSocket message: {}", e.getMessage());
        }
    }
    
    /**
     * Handle ticker update from WebSocket
     */
    private void handleTickerUpdate(JsonNode message) {
        try {
            JsonNode data = message.get("data");
            if (data == null) return;
            
            String symbol = data.get("symbol").asText();
            Consumer<TickerData> callback = subscriptions.get(symbol);
            
            if (callback != null) {
                TickerData tickerData = parseWebSocketTickerData(data, symbol);
                callback.accept(tickerData);
                log.debug("Processed WebSocket ticker update for {}", symbol);
            }
            
        } catch (Exception e) {
            log.error("Failed to handle ticker update: {}", e.getMessage());
        }
    }
    
    /**
     * Parse ticker data from WebSocket message
     */
    private TickerData parseWebSocketTickerData(JsonNode data, String symbol) {
        return TickerData.builder()
                .symbol(restoreSymbolFormat(symbol))
                .exchange(getExchangeName())
                .price(new BigDecimal(data.get("lastPrice").asText()))
                .bid(new BigDecimal(data.get("bid1Price").asText()))
                .ask(new BigDecimal(data.get("ask1Price").asText()))
                .volume24h(new BigDecimal(data.get("volume24h").asText()))
                .change24h(new BigDecimal(data.get("price24hPcnt").asText()).multiply(BigDecimal.valueOf(100)))
                .timestamp(LocalDateTime.now())
                .status(TickerData.TickerStatus.ACTIVE)
                .build();
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
        
        return executeRequest(
                webClient.get()
                        .uri("/v5/market/instruments-info", uriBuilder -> uriBuilder
                                .queryParam("category", "spot")
                                .build())
                        .retrieve()
                        .bodyToMono(JsonNode.class),
                JsonNode.class
        ).map(this::parseSymbolsResponse)
         .doOnNext(symbols -> {
             if (cacheService != null) {
                 cacheService.cacheSymbols(getExchangeName(), symbols);
             }
         });
    }
    
    @Override
    protected String getHealthCheckEndpoint() {
        return "/v5/market/time";
    }
    
    @Override
    protected String normalizeSymbol(String symbol) {
        // Bybit использует формат BTCUSDT (без слеша)
        return symbol.replace("/", "").toUpperCase();
    }
    
    /**
     * Парсить ответ API с данными тикера
     */
    private TickerData parseTickerResponse(JsonNode response) {
        try {
            JsonNode result = response.get("result");
            if (result == null || !result.has("list") || result.get("list").size() == 0) {
                throw new RuntimeException("Invalid response format");
            }
            
            JsonNode tickerData = result.get("list").get(0);
            
            return TickerData.builder()
                    .symbol(restoreSymbolFormat(tickerData.get("symbol").asText()))
                    .exchange(getExchangeName())
                    .price(new BigDecimal(tickerData.get("lastPrice").asText()))
                    .bid(new BigDecimal(tickerData.get("bid1Price").asText()))
                    .ask(new BigDecimal(tickerData.get("ask1Price").asText()))
                    .volume24h(new BigDecimal(tickerData.get("volume24h").asText()))
                    .change24h(new BigDecimal(tickerData.get("price24hPcnt").asText()).multiply(BigDecimal.valueOf(100)))
                    .timestamp(LocalDateTime.now())
                    .status(TickerData.TickerStatus.ACTIVE)
                    .build();
                    
        } catch (Exception e) {
            log.error("Failed to parse Bybit ticker response: {}", e.getMessage());
            throw new RuntimeException("Failed to parse ticker data", e);
        }
    }
    
    /**
     * Парсить список поддерживаемых символов
     */
    private List<String> parseSymbolsResponse(JsonNode response) {
        try {
            JsonNode result = response.get("result");
            if (result == null || !result.has("list")) {
                throw new RuntimeException("Invalid symbols response format");
            }
            
            return result.get("list").findValuesAsText("symbol")
                    .stream()
                    .map(this::restoreSymbolFormat)
                    .toList();
                    
        } catch (Exception e) {
            log.error("Failed to parse Bybit symbols response: {}", e.getMessage());
            throw new RuntimeException("Failed to parse symbols data", e);
        }
    }
    
    /**
     * Восстановить формат символа с слешем (BTCUSDT -> BTC/USDT)
     */
    private String restoreSymbolFormat(String symbol) {
        // Простая логика для основных валют
        if (symbol.endsWith("USDT")) {
            return symbol.substring(0, symbol.length() - 4) + "/USDT";
        } else if (symbol.endsWith("USDC")) {
            return symbol.substring(0, symbol.length() - 4) + "/USDC";
        } else if (symbol.endsWith("BTC")) {
            return symbol.substring(0, symbol.length() - 3) + "/BTC";
        } else if (symbol.endsWith("ETH")) {
            return symbol.substring(0, symbol.length() - 3) + "/ETH";
        }
        return symbol; // Возвращаем как есть если не смогли распарсить
    }
} 