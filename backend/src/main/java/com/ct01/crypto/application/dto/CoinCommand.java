package com.ct01.crypto.application.dto;

import com.ct01.crypto.domain.TrackedCoin;

import java.util.Set;

/**
 * Команды для операций с монетами в application слое
 */
public class CoinCommand {
    
    /**
     * Команда для создания отслеживаемой монеты
     */
    public record CreateTrackedCoinCommand(
            String symbol,
            String name,
            Set<TrackedCoin.Exchange> exchanges,
            Set<String> quoteCurrencies,
            Boolean isActive,
            Integer pollingIntervalSeconds,
            Boolean websocketEnabled,
            Integer priority,
            String notes
    ) {
        public CreateTrackedCoinCommand {
            if (symbol == null || symbol.trim().isEmpty()) {
                throw new IllegalArgumentException("Symbol cannot be null or empty");
            }
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Name cannot be null or empty");
            }
            if (exchanges == null || exchanges.isEmpty()) {
                throw new IllegalArgumentException("At least one exchange must be specified");
            }
            if (quoteCurrencies == null || quoteCurrencies.isEmpty()) {
                throw new IllegalArgumentException("At least one quote currency must be specified");
            }
        }
    }
    
    /**
     * Команда для обновления отслеживаемой монеты
     */
    public record UpdateTrackedCoinCommand(
            Long id,
            String symbol,
            String name,
            Set<TrackedCoin.Exchange> exchanges,
            Set<String> quoteCurrencies,
            Boolean isActive,
            Integer pollingIntervalSeconds,
            Boolean websocketEnabled,
            Integer priority,
            String notes
    ) {
        public UpdateTrackedCoinCommand {
            if (id == null) {
                throw new IllegalArgumentException("ID cannot be null");
            }
            if (symbol != null && symbol.trim().isEmpty()) {
                throw new IllegalArgumentException("Symbol cannot be empty");
            }
            if (name != null && name.trim().isEmpty()) {
                throw new IllegalArgumentException("Name cannot be empty");
            }
        }
    }
    
    /**
     * Команда для массового изменения статуса активности
     */
    public record BulkActivationCommand(
            Set<String> symbols,
            boolean isActive
    ) {
        public BulkActivationCommand {
            if (symbols == null || symbols.isEmpty()) {
                throw new IllegalArgumentException("Symbols cannot be null or empty");
            }
        }
    }
    
    /**
     * Команда для переключения активности монеты
     */
    public record ToggleActivationCommand(
            Long id,
            boolean isActive
    ) {
        public ToggleActivationCommand {
            if (id == null) {
                throw new IllegalArgumentException("ID cannot be null");
            }
        }
    }
} 
