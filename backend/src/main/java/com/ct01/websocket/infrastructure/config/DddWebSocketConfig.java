package com.ct01.websocket.infrastructure.config;

import com.ct01.websocket.infrastructure.handler.DddWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * DDD WebSocket конфигурация
 * Новая архитектура для замены legacy PriceWebSocketController
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class DddWebSocketConfig implements WebSocketConfigurer {
    
    private final DddWebSocketHandler dddWebSocketHandler;
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Регистрируем новый DDD WebSocket handler
        registry.addHandler(dddWebSocketHandler, "/ws/ddd/prices")
                .setAllowedOrigins("*"); // TODO: Настроить CORS для production
        
        // Для обратной совместимости, также регистрируем на старом пути
        registry.addHandler(dddWebSocketHandler, "/ws/prices/v2")
                .setAllowedOrigins("*");
    }
} 