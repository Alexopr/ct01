package com.ct01.market.application.usecase;

import com.ct01.core.application.UseCase;
import com.ct01.market.domain.*;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Use Case для обновления рыночных данных
 */
public class UpdateMarketDataUseCase implements UseCase<UpdateMarketDataUseCase.Command, UpdateMarketDataUseCase.Result> {
    
    private final MarketDataRepository marketDataRepository;
    
    public UpdateMarketDataUseCase(MarketDataRepository marketDataRepository) {
        this.marketDataRepository = marketDataRepository;
    }
    
    @Override
    public Result execute(Command command) {
        try {
            switch (command.getType()) {
                case UPDATE_PRICES:
                    return handleUpdatePrices(command);
                case MARK_AS_STALE:
                    return handleMarkAsStale(command);
                case MARK_AS_ERROR:
                    return handleMarkAsError(command);
                case CREATE_OR_UPDATE:
                    return handleCreateOrUpdate(command);
                default:
                    throw new IllegalArgumentException("Unsupported command type: " + command.getType());
            }
        } catch (Exception e) {
            return Result.failure("Failed to update market data: " + e.getMessage());
        }
    }
    
    private Result handleUpdatePrices(Command command) {
        Optional<MarketData> existingData = marketDataRepository.findBySymbolAndExchange(
            command.getSymbol(), command.getExchange());
        
        if (existingData.isEmpty()) {
            return Result.failure("Market data not found for symbol: " + command.getSymbol() + 
                                 " and exchange: " + command.getExchange());
        }
        
        MarketData updated = existingData.get().updatePrices(
            command.getCurrentPrice(),
            command.getBidPrice(),
            command.getAskPrice(),
            command.getVolume24h(),
            command.getChange24h()
        );
        
        MarketData saved = marketDataRepository.save(updated);
        return Result.success(saved, "Market data prices updated successfully");
    }
    
    private Result handleMarkAsStale(Command command) {
        Optional<MarketData> existingData = marketDataRepository.findBySymbolAndExchange(
            command.getSymbol(), command.getExchange());
        
        if (existingData.isEmpty()) {
            return Result.failure("Market data not found for symbol: " + command.getSymbol() + 
                                 " and exchange: " + command.getExchange());
        }
        
        MarketData staleData = existingData.get().markAsStale();
        MarketData saved = marketDataRepository.save(staleData);
        return Result.success(saved, "Market data marked as stale");
    }
    
    private Result handleMarkAsError(Command command) {
        Optional<MarketData> existingData = marketDataRepository.findBySymbolAndExchange(
            command.getSymbol(), command.getExchange());
        
        if (existingData.isEmpty()) {
            return Result.failure("Market data not found for symbol: " + command.getSymbol() + 
                                 " and exchange: " + command.getExchange());
        }
        
        MarketData errorData = existingData.get().markAsError(command.getErrorMessage());
        MarketData saved = marketDataRepository.save(errorData);
        return Result.success(saved, "Market data marked as error");
    }
    
    private Result handleCreateOrUpdate(Command command) {
        MarketDataId id = MarketDataId.fromSymbolAndExchange(command.getSymbol(), command.getExchange());
        
        Optional<MarketData> existingData = marketDataRepository.findBySymbolAndExchange(
            command.getSymbol(), command.getExchange());
        
        MarketData marketData;
        String message;
        
        if (existingData.isPresent()) {
            // Обновляем существующие данные
            marketData = existingData.get().updatePrices(
                command.getCurrentPrice(),
                command.getBidPrice(),
                command.getAskPrice(),
                command.getVolume24h(),
                command.getChange24h()
            );
            message = "Market data updated successfully";
        } else {
            // Создаем новые данные
            marketData = new MarketData(
                id,
                command.getSymbol(),
                command.getExchange(),
                command.getCurrentPrice(),
                command.getBidPrice(),
                command.getAskPrice(),
                command.getVolume24h(),
                command.getChange24h(),
                LocalDateTime.now(),
                MarketDataStatus.ACTIVE,
                null
            );
            message = "Market data created successfully";
        }
        
        MarketData saved = marketDataRepository.save(marketData);
        return Result.success(saved, message);
    }
    
    /**
     * Command для обновления рыночных данных
     */
    public static class Command {
        private final CommandType type;
        private final String symbol;
        private final String exchange;
        private final Price currentPrice;
        private final Price bidPrice;
        private final Price askPrice;
        private final Volume volume24h;
        private final PriceChange change24h;
        private final String errorMessage;
        
        private Command(CommandType type, String symbol, String exchange, Price currentPrice,
                       Price bidPrice, Price askPrice, Volume volume24h, PriceChange change24h,
                       String errorMessage) {
            this.type = type;
            this.symbol = symbol;
            this.exchange = exchange;
            this.currentPrice = currentPrice;
            this.bidPrice = bidPrice;
            this.askPrice = askPrice;
            this.volume24h = volume24h;
            this.change24h = change24h;
            this.errorMessage = errorMessage;
        }
        
        public static Command updatePrices(String symbol, String exchange, Price currentPrice,
                                         Price bidPrice, Price askPrice, Volume volume24h, PriceChange change24h) {
            return new Command(CommandType.UPDATE_PRICES, symbol, exchange, currentPrice,
                             bidPrice, askPrice, volume24h, change24h, null);
        }
        
        public static Command markAsStale(String symbol, String exchange) {
            return new Command(CommandType.MARK_AS_STALE, symbol, exchange, null,
                             null, null, null, null, null);
        }
        
        public static Command markAsError(String symbol, String exchange, String errorMessage) {
            return new Command(CommandType.MARK_AS_ERROR, symbol, exchange, null,
                             null, null, null, null, errorMessage);
        }
        
        public static Command createOrUpdate(String symbol, String exchange, Price currentPrice,
                                           Price bidPrice, Price askPrice, Volume volume24h, PriceChange change24h) {
            return new Command(CommandType.CREATE_OR_UPDATE, symbol, exchange, currentPrice,
                             bidPrice, askPrice, volume24h, change24h, null);
        }
        
        // Getters
        public CommandType getType() { return type; }
        public String getSymbol() { return symbol; }
        public String getExchange() { return exchange; }
        public Price getCurrentPrice() { return currentPrice; }
        public Price getBidPrice() { return bidPrice; }
        public Price getAskPrice() { return askPrice; }
        public Volume getVolume24h() { return volume24h; }
        public PriceChange getChange24h() { return change24h; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    /**
     * Result для обновления рыночных данных
     */
    public static class Result {
        private final boolean success;
        private final MarketData data;
        private final String message;
        private final String errorMessage;
        
        private Result(boolean success, MarketData data, String message, String errorMessage) {
            this.success = success;
            this.data = data;
            this.message = message;
            this.errorMessage = errorMessage;
        }
        
        public static Result success(MarketData data, String message) {
            return new Result(true, data, message, null);
        }
        
        public static Result failure(String errorMessage) {
            return new Result(false, null, null, errorMessage);
        }
        
        public boolean isSuccess() { return success; }
        public boolean isFailure() { return !success; }
        public MarketData getData() { return data; }
        public String getMessage() { return message; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    /**
     * Типы команд
     */
    public enum CommandType {
        UPDATE_PRICES,
        MARK_AS_STALE,
        MARK_AS_ERROR,
        CREATE_OR_UPDATE
    }
} 
