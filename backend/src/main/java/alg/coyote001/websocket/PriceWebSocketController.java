package alg.coyote001.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket handler for real-time price updates
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PriceWebSocketController implements WebSocketHandler {
    
    private final ObjectMapper objectMapper;
    
    // Store active WebSocket sessions
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    // Store subscriptions by session ID
    private final Map<String, Set<String>> subscriptions = new ConcurrentHashMap<>();
    
    // Store subscriptions by symbol (for broadcasting)
    private final Map<String, Set<String>> symbolSubscriptions = new ConcurrentHashMap<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        subscriptions.put(sessionId, new CopyOnWriteArraySet<>());
        
        log.info("WebSocket connection established: {}", sessionId);
        
        // Send welcome message
        Map<String, Object> welcomeMessage = Map.of(
            "type", "welcome",
            "sessionId", sessionId,
            "message", "Connected to price updates service"
        );
        
        sendMessage(session, welcomeMessage);
    }
    
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String sessionId = session.getId();
        
        try {
            String payload = (String) message.getPayload();
            Map<String, Object> request = objectMapper.readValue(payload, Map.class);
            
            String action = (String) request.get("action");
            
            switch (action) {
                case "subscribe":
                    handleSubscribe(session, request);
                    break;
                case "unsubscribe":
                    handleUnsubscribe(session, request);
                    break;
                case "ping":
                    handlePing(session);
                    break;
                default:
                    sendError(session, "Unknown action: " + action);
            }
            
        } catch (JsonProcessingException e) {
            log.error("Error parsing WebSocket message from session {}: {}", sessionId, e.getMessage());
            sendError(session, "Invalid JSON format");
        } catch (Exception e) {
            log.error("Error handling WebSocket message from session {}: {}", sessionId, e.getMessage());
            sendError(session, "Internal server error");
        }
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String sessionId = session.getId();
        log.error("WebSocket transport error for session {}: {}", sessionId, exception.getMessage());
        
        cleanupSession(sessionId);
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String sessionId = session.getId();
        log.info("WebSocket connection closed: {} with status: {}", sessionId, closeStatus);
        
        cleanupSession(sessionId);
    }
    
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
    
    /**
     * Handle symbol subscription
     */
    private void handleSubscribe(WebSocketSession session, Map<String, Object> request) {
        String sessionId = session.getId();
        
        Object symbolsObj = request.get("symbols");
        if (symbolsObj == null) {
            sendError(session, "Symbols array is required for subscription");
            return;
        }
        
        List<String> symbols;
        if (symbolsObj instanceof List) {
            symbols = (List<String>) symbolsObj;
        } else {
            symbols = List.of((String) symbolsObj);
        }
        
        Set<String> sessionSubscriptions = subscriptions.get(sessionId);
        
        for (String symbol : symbols) {
            String normalizedSymbol = symbol.toUpperCase();
            
            // Add to session subscriptions
            sessionSubscriptions.add(normalizedSymbol);
            
            // Add to symbol subscriptions
            symbolSubscriptions.computeIfAbsent(normalizedSymbol, k -> new CopyOnWriteArraySet<>())
                    .add(sessionId);
        }
        
        Map<String, Object> response = Map.of(
            "type", "subscription_confirmed",
            "symbols", symbols,
            "totalSubscriptions", sessionSubscriptions.size()
        );
        
        sendMessage(session, response);
        
        log.info("Session {} subscribed to symbols: {}", sessionId, symbols);
    }
    
    /**
     * Handle symbol unsubscription
     */
    private void handleUnsubscribe(WebSocketSession session, Map<String, Object> request) {
        String sessionId = session.getId();
        
        Object symbolsObj = request.get("symbols");
        if (symbolsObj == null) {
            sendError(session, "Symbols array is required for unsubscription");
            return;
        }
        
        List<String> symbols;
        if (symbolsObj instanceof List) {
            symbols = (List<String>) symbolsObj;
        } else {
            symbols = List.of((String) symbolsObj);
        }
        
        Set<String> sessionSubscriptions = subscriptions.get(sessionId);
        
        for (String symbol : symbols) {
            String normalizedSymbol = symbol.toUpperCase();
            
            // Remove from session subscriptions
            sessionSubscriptions.remove(normalizedSymbol);
            
            // Remove from symbol subscriptions
            Set<String> symbolSubs = symbolSubscriptions.get(normalizedSymbol);
            if (symbolSubs != null) {
                symbolSubs.remove(sessionId);
                if (symbolSubs.isEmpty()) {
                    symbolSubscriptions.remove(normalizedSymbol);
                }
            }
        }
        
        Map<String, Object> response = Map.of(
            "type", "unsubscription_confirmed",
            "symbols", symbols,
            "totalSubscriptions", sessionSubscriptions.size()
        );
        
        sendMessage(session, response);
        
        log.info("Session {} unsubscribed from symbols: {}", sessionId, symbols);
    }
    
    /**
     * Handle ping request
     */
    private void handlePing(WebSocketSession session) {
        Map<String, Object> response = Map.of(
            "type", "pong",
            "timestamp", System.currentTimeMillis()
        );
        
        sendMessage(session, response);
    }
    
    /**
     * Broadcast price update to all subscribed sessions
     */
    public void broadcastPriceUpdate(String symbol, Map<String, Object> priceData) {
        String normalizedSymbol = symbol.toUpperCase();
        Set<String> subscribedSessions = symbolSubscriptions.get(normalizedSymbol);
        
        if (subscribedSessions == null || subscribedSessions.isEmpty()) {
            return;
        }
        
        Map<String, Object> message = Map.of(
            "type", "price_update",
            "symbol", normalizedSymbol,
            "data", priceData,
            "timestamp", System.currentTimeMillis()
        );
        
        // Create a copy to avoid concurrent modification
        List<String> sessionIds = new ArrayList<>(subscribedSessions);
        
        for (String sessionId : sessionIds) {
            WebSocketSession session = sessions.get(sessionId);
            if (session != null && session.isOpen()) {
                sendMessage(session, message);
            } else {
                // Clean up dead session
                cleanupSession(sessionId);
            }
        }
        
        log.debug("Broadcasted price update for {} to {} sessions", normalizedSymbol, sessionIds.size());
    }
    
    /**
     * Send message to a specific session
     */
    private void sendMessage(WebSocketSession session, Map<String, Object> message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        } catch (JsonProcessingException e) {
            log.error("Error serializing message: {}", e.getMessage());
        } catch (IOException e) {
            log.error("Error sending WebSocket message to session {}: {}", session.getId(), e.getMessage());
        }
    }
    
    /**
     * Send error message to session
     */
    private void sendError(WebSocketSession session, String error) {
        Map<String, Object> errorMessage = Map.of(
            "type", "error",
            "message", error,
            "timestamp", System.currentTimeMillis()
        );
        
        sendMessage(session, errorMessage);
    }
    
    /**
     * Clean up session data
     */
    private void cleanupSession(String sessionId) {
        sessions.remove(sessionId);
        
        Set<String> sessionSubscriptions = subscriptions.remove(sessionId);
        if (sessionSubscriptions != null) {
            // Remove session from all symbol subscriptions
            for (String symbol : sessionSubscriptions) {
                Set<String> symbolSubs = symbolSubscriptions.get(symbol);
                if (symbolSubs != null) {
                    symbolSubs.remove(sessionId);
                    if (symbolSubs.isEmpty()) {
                        symbolSubscriptions.remove(symbol);
                    }
                }
            }
        }
        
        log.info("Cleaned up session: {}", sessionId);
    }
    
    /**
     * Get connection statistics
     */
    public Map<String, Object> getConnectionStats() {
        return Map.of(
            "totalSessions", sessions.size(),
            "totalSubscriptions", symbolSubscriptions.size(),
            "activeSymbols", symbolSubscriptions.keySet()
        );
    }
} 