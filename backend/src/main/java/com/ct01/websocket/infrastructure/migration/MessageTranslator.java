package com.ct01.websocket.infrastructure.migration;

import com.ct01.websocket.domain.message.MessageType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Транслятор сообщений между legacy форматом и новой DDD архитектурой
 */
@Component
public class MessageTranslator {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageTranslator.class);
    private final ObjectMapper objectMapper;
    
    public MessageTranslator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * Переводит legacy сообщение в новый формат
     */
    public TranslatedMessage translateFromLegacy(JsonNode legacyMessage) {
        try {
            String action = legacyMessage.path("action").asText();
            JsonNode data = legacyMessage.path("data");
            
            return switch (action.toLowerCase()) {
                case "subscribe" -> createSubscribeMessage(data);
                case "unsubscribe" -> createUnsubscribeMessage(data);
                case "ping" -> createPingMessage();
                default -> {
                    logger.warn("Unknown legacy action: {}", action);
                    yield null;
                }
            };
            
        } catch (Exception e) {
            logger.error("Failed to translate legacy message", e);
            return null;
        }
    }
    
    /**
     * Переводит сообщение из новой системы в legacy формат
     */
    public String translateToLegacy(MessageType messageType, Object payload) {
        try {
            ObjectNode legacyMessage = objectMapper.createObjectNode();
            
            switch (messageType) {
                case WELCOME -> {
                    legacyMessage.put("type", "connected");
                    legacyMessage.set("data", objectMapper.valueToTree(payload));
                }
                case PRICE_UPDATE -> {
                    legacyMessage.put("type", "price");
                    legacyMessage.set("data", objectMapper.valueToTree(payload));
                }
                case SUBSCRIPTION_CONFIRMED -> {
                    legacyMessage.put("type", "subscribed");
                    legacyMessage.set("data", objectMapper.valueToTree(payload));
                }
                case SUBSCRIPTION_FAILED -> {
                    legacyMessage.put("type", "error");
                    legacyMessage.set("error", objectMapper.valueToTree(payload));
                }
                case ERROR -> {
                    legacyMessage.put("type", "error");
                    legacyMessage.set("error", objectMapper.valueToTree(payload));
                }
                case HEARTBEAT -> {
                    legacyMessage.put("type", "pong");
                    legacyMessage.put("timestamp", System.currentTimeMillis());
                }
                default -> {
                    logger.warn("Unknown message type for legacy translation: {}", messageType);
                    return null;
                }
            }
            
            return objectMapper.writeValueAsString(legacyMessage);
            
        } catch (Exception e) {
            logger.error("Failed to translate to legacy format", e);
            return null;
        }
    }
    
    private TranslatedMessage createSubscribeMessage(JsonNode data) {
        String symbol = data.path("symbol").asText();
        if (symbol.isEmpty()) {
            logger.warn("Missing symbol in subscribe message");
            return null;
        }
        
        return new TranslatedMessage(TranslatedMessageType.SUBSCRIBE, symbol.toUpperCase());
    }
    
    private TranslatedMessage createUnsubscribeMessage(JsonNode data) {
        String symbol = data.path("symbol").asText();
        if (symbol.isEmpty()) {
            logger.warn("Missing symbol in unsubscribe message");
            return null;
        }
        
        return new TranslatedMessage(TranslatedMessageType.UNSUBSCRIBE, symbol.toUpperCase());
    }
    
    private TranslatedMessage createPingMessage() {
        return new TranslatedMessage(TranslatedMessageType.PING, System.currentTimeMillis());
    }
}

/**
 * Представляет транслированное сообщение
 */
class TranslatedMessage {
    private final TranslatedMessageType type;
    private final Object payload;
    
    public TranslatedMessage(TranslatedMessageType type, Object payload) {
        this.type = type;
        this.payload = payload;
    }
    
    public TranslatedMessageType getType() {
        return type;
    }
    
    public Object getPayload() {
        return payload;
    }
}

/**
 * Типы транслированных сообщений
 */
enum TranslatedMessageType {
    SUBSCRIBE,
    UNSUBSCRIBE,
    PING
} 