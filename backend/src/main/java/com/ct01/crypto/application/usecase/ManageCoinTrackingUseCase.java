package com.ct01.crypto.application.usecase;

import com.ct01.crypto.application.dto.CoinCommand;
import com.ct01.crypto.application.dto.CoinResult;
import com.ct01.crypto.domain.TrackedCoin;
import com.ct01.crypto.domain.repository.TrackedCoinRepository;
import com.ct01.crypto.domain.service.CoinDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Use Case для управления отслеживаемыми криптовалютами
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ManageCoinTrackingUseCase {
    
    private final TrackedCoinRepository trackedCoinRepository;
    private final CoinDomainService coinDomainService;
    
    /**
     * Создать новую отслеживаемую монету
     */
    public CoinResult.TrackedCoinOperationResult createTrackedCoin(CoinCommand.CreateTrackedCoinCommand command) {
        log.info("Creating tracked coin: {}", command.symbol());
        
        try {
            // Проверка уникальности символа
            if (trackedCoinRepository.existsBySymbol(command.symbol())) {
                return CoinResult.TrackedCoinOperationResult.failure(
                    "Coin with symbol " + command.symbol() + " already exists",
                    "DUPLICATE_SYMBOL"
                );
            }
            
            // Создание доменной модели
            TrackedCoin trackedCoin = new TrackedCoin(
                null, // ID будет назначен при сохранении
                command.symbol().toUpperCase(),
                command.name(),
                command.exchanges(),
                normalizeQuoteCurrencies(command.quoteCurrencies()),
                command.isActive() != null ? command.isActive() : true,
                command.pollingIntervalSeconds(),
                command.websocketEnabled() != null ? command.websocketEnabled() : false,
                command.priority() != null ? command.priority() : 1,
                command.notes(),
                LocalDateTime.now(),
                LocalDateTime.now()
            );
            
            // Валидация через доменный сервис
            boolean isValid = coinDomainService.validateCoinData(trackedCoin.getSymbol(), trackedCoin.getName());
            if (!isValid) {
                return CoinResult.TrackedCoinOperationResult.failure(
                    "Invalid coin data",
                    "VALIDATION_ERROR"
                );
            }
            
            // Сохранение
            TrackedCoin savedCoin = trackedCoinRepository.save(trackedCoin);
            
            log.info("Successfully created tracked coin: {} with ID: {}", savedCoin.getSymbol(), savedCoin.getId());
            return CoinResult.TrackedCoinOperationResult.success(savedCoin, "Tracked coin created successfully");
            
        } catch (Exception e) {
            log.error("Failed to create tracked coin: {}", e.getMessage(), e);
            return CoinResult.TrackedCoinOperationResult.failure(
                "Failed to create tracked coin: " + e.getMessage(),
                "CREATION_ERROR"
            );
        }
    }
    
    /**
     * Обновить отслеживаемую монету
     */
    public CoinResult.TrackedCoinOperationResult updateTrackedCoin(CoinCommand.UpdateTrackedCoinCommand command) {
        log.info("Updating tracked coin with ID: {}", command.id());
        
        try {
            // Найти существующую монету
            TrackedCoin existingCoin = trackedCoinRepository.findById(command.id())
                .orElse(null);
            
            if (existingCoin == null) {
                return CoinResult.TrackedCoinOperationResult.failure(
                    "Tracked coin not found with ID: " + command.id(),
                    "NOT_FOUND"
                );
            }
            
            // Проверка уникальности символа при изменении
            if (command.symbol() != null && !existingCoin.getSymbol().equalsIgnoreCase(command.symbol())) {
                if (trackedCoinRepository.existsBySymbol(command.symbol())) {
                    return CoinResult.TrackedCoinOperationResult.failure(
                        "Coin with symbol " + command.symbol() + " already exists",
                        "DUPLICATE_SYMBOL"
                    );
                }
            }
            
            // Обновление полей
            TrackedCoin updatedCoin = new TrackedCoin(
                existingCoin.getId(),
                command.symbol() != null ? command.symbol().toUpperCase() : existingCoin.getSymbol(),
                command.name() != null ? command.name() : existingCoin.getName(),
                command.exchanges() != null ? command.exchanges() : existingCoin.getExchanges(),
                command.quoteCurrencies() != null ? normalizeQuoteCurrencies(command.quoteCurrencies()) : existingCoin.getQuoteCurrencies(),
                command.isActive() != null ? command.isActive() : existingCoin.getIsActive(),
                command.pollingIntervalSeconds() != null ? command.pollingIntervalSeconds() : existingCoin.getPollingIntervalSeconds(),
                command.websocketEnabled() != null ? command.websocketEnabled() : existingCoin.getWebsocketEnabled(),
                command.priority() != null ? command.priority() : existingCoin.getPriority(),
                command.notes() != null ? command.notes() : existingCoin.getNotes(),
                existingCoin.getCreatedAt(),
                LocalDateTime.now()
            );
            
            // Сохранение
            TrackedCoin savedCoin = trackedCoinRepository.save(updatedCoin);
            
            log.info("Successfully updated tracked coin: {}", savedCoin.getSymbol());
            return CoinResult.TrackedCoinOperationResult.success(savedCoin, "Tracked coin updated successfully");
            
        } catch (Exception e) {
            log.error("Failed to update tracked coin: {}", e.getMessage(), e);
            return CoinResult.TrackedCoinOperationResult.failure(
                "Failed to update tracked coin: " + e.getMessage(),
                "UPDATE_ERROR"
            );
        }
    }
    
    /**
     * Переключить активность монеты
     */
    public CoinResult.TrackedCoinOperationResult toggleActivation(CoinCommand.ToggleActivationCommand command) {
        log.info("Toggling activation for tracked coin ID: {} to: {}", command.id(), command.isActive());
        
        try {
            TrackedCoin coin = trackedCoinRepository.findById(command.id())
                .orElse(null);
            
            if (coin == null) {
                return CoinResult.TrackedCoinOperationResult.failure(
                    "Tracked coin not found with ID: " + command.id(),
                    "NOT_FOUND"
                );
            }
            
            // Использование доменного метода
            TrackedCoin updatedCoin = command.isActive() ? coin.activate() : coin.deactivate();
            TrackedCoin savedCoin = trackedCoinRepository.save(updatedCoin);
            
            log.info("Successfully toggled activation for coin: {} to: {}", savedCoin.getSymbol(), command.isActive());
            return CoinResult.TrackedCoinOperationResult.success(savedCoin, "Activation status updated successfully");
            
        } catch (Exception e) {
            log.error("Failed to toggle activation: {}", e.getMessage(), e);
            return CoinResult.TrackedCoinOperationResult.failure(
                "Failed to toggle activation: " + e.getMessage(),
                "TOGGLE_ERROR"
            );
        }
    }
    
    /**
     * Массовое изменение активности монет
     */
    public CoinResult.BulkOperationResult bulkToggleActivation(CoinCommand.BulkActivationCommand command) {
        log.info("Bulk toggling activation for {} symbols to: {}", command.symbols().size(), command.isActive());
        
        List<TrackedCoin> processedCoins = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (String symbol : command.symbols()) {
            try {
                TrackedCoin coin = trackedCoinRepository.findBySymbol(symbol)
                    .orElse(null);
                
                if (coin == null) {
                    errors.add("Coin not found: " + symbol);
                    continue;
                }
                
                TrackedCoin updatedCoin = command.isActive() ? coin.activate() : coin.deactivate();
                TrackedCoin savedCoin = trackedCoinRepository.save(updatedCoin);
                processedCoins.add(savedCoin);
                
            } catch (Exception e) {
                errors.add("Failed to process " + symbol + ": " + e.getMessage());
                log.error("Failed to process symbol {}: {}", symbol, e.getMessage(), e);
            }
        }
        
        log.info("Bulk operation completed. Processed: {}, Failed: {}", processedCoins.size(), errors.size());
        
        if (errors.isEmpty()) {
            return CoinResult.BulkOperationResult.success(processedCoins, "All coins processed successfully");
        } else if (!processedCoins.isEmpty()) {
            return CoinResult.BulkOperationResult.partial(processedCoins, errors.size(), errors);
        } else {
            return CoinResult.BulkOperationResult.failure("No coins were processed", errors);
        }
    }
    
    /**
     * Удалить отслеживаемую монету
     */
    public CoinResult.TrackedCoinOperationResult deleteTrackedCoin(Long id) {
        log.info("Deleting tracked coin with ID: {}", id);
        
        try {
            TrackedCoin coin = trackedCoinRepository.findById(id)
                .orElse(null);
            
            if (coin == null) {
                return CoinResult.TrackedCoinOperationResult.failure(
                    "Tracked coin not found with ID: " + id,
                    "NOT_FOUND"
                );
            }
            
            trackedCoinRepository.delete(coin);
            
            log.info("Successfully deleted tracked coin: {}", coin.getSymbol());
            return CoinResult.TrackedCoinOperationResult.success(coin, "Tracked coin deleted successfully");
            
        } catch (Exception e) {
            log.error("Failed to delete tracked coin: {}", e.getMessage(), e);
            return CoinResult.TrackedCoinOperationResult.failure(
                "Failed to delete tracked coin: " + e.getMessage(),
                "DELETE_ERROR"
            );
        }
    }
    
    /**
     * Нормализация котируемых валют
     */
    private Set<String> normalizeQuoteCurrencies(Set<String> quoteCurrencies) {
        return quoteCurrencies.stream()
            .map(String::toUpperCase)
            .collect(java.util.stream.Collectors.toSet());
    }
} 
