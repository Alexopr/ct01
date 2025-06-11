package com.ct01.websocket.application.command;

import com.ct01.shared.application.Command;
import com.ct01.websocket.domain.session.SessionId;

import java.util.List;

/**
 * Command для подписки на символы криптовалют
 */
public record SubscribeToSymbolCommand(
    SessionId sessionId,
    List<String> symbols
) implements Command {
    
    public SubscribeToSymbolCommand {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
        if (symbols == null || symbols.isEmpty()) {
            throw new IllegalArgumentException("Symbols list cannot be null or empty");
        }
        
        // Валидируем символы
        for (String symbol : symbols) {
            if (symbol == null || symbol.trim().isEmpty()) {
                throw new IllegalArgumentException("Symbol cannot be null or empty");
            }
        }
    }
    
    /**
     * Создать команду для одного символа
     */
    public static SubscribeToSymbolCommand single(SessionId sessionId, String symbol) {
        return new SubscribeToSymbolCommand(sessionId, List.of(symbol));
    }
    
    /**
     * Получить нормализованные символы (uppercase)
     */
    public List<String> getNormalizedSymbols() {
        return symbols.stream()
                .map(String::trim)
                .map(String::toUpperCase)
                .toList();
    }
} 