package com.ct01.market.application.usecase;

import com.ct01.core.application.UseCase;
import com.ct01.market.domain.MarketData;
import com.ct01.market.domain.MarketDataRepository;
import com.ct01.market.domain.MarketDataStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Use Case для получения рыночных данных
 */
public class GetMarketDataUseCase implements UseCase<GetMarketDataUseCase.Query, GetMarketDataUseCase.Result> {
    
    private final MarketDataRepository marketDataRepository;
    
    public GetMarketDataUseCase(MarketDataRepository marketDataRepository) {
        this.marketDataRepository = marketDataRepository;
    }
    
    @Override
    public Result execute(Query query) {
        switch (query.getType()) {
            case BY_SYMBOL_AND_EXCHANGE:
                return handleBySymbolAndExchange(query);
            case BY_SYMBOL:
                return handleBySymbol(query);
            case BY_EXCHANGE:
                return handleByExchange(query);
            case BY_STATUS:
                return handleByStatus(query);
            case ACTIVE_DATA:
                return handleActiveData();
            case STALE_DATA:
                return handleStaleData(query);
            case UPDATED_AFTER:
                return handleUpdatedAfter(query);
            default:
                throw new IllegalArgumentException("Unsupported query type: " + query.getType());
        }
    }
    
    private Result handleBySymbolAndExchange(Query query) {
        Optional<MarketData> data = marketDataRepository.findBySymbolAndExchange(
            query.getSymbol(), query.getExchange());
        return data.map(d -> Result.single(d))
                  .orElse(Result.empty());
    }
    
    private Result handleBySymbol(Query query) {
        List<MarketData> data = marketDataRepository.findAllBySymbol(query.getSymbol());
        return Result.multiple(data);
    }
    
    private Result handleByExchange(Query query) {
        List<MarketData> data = marketDataRepository.findAllByExchange(query.getExchange());
        return Result.multiple(data);
    }
    
    private Result handleByStatus(Query query) {
        List<MarketData> data = marketDataRepository.findByStatus(query.getStatus());
        return Result.multiple(data);
    }
    
    private Result handleActiveData() {
        List<MarketData> data = marketDataRepository.findActiveData();
        return Result.multiple(data);
    }
    
    private Result handleStaleData(Query query) {
        List<MarketData> data = marketDataRepository.findStaleData(query.getOlderThan());
        return Result.multiple(data);
    }
    
    private Result handleUpdatedAfter(Query query) {
        List<MarketData> data = marketDataRepository.findUpdatedAfter(query.getUpdatedAfter());
        return Result.multiple(data);
    }
    
    /**
     * Query для получения рыночных данных
     */
    public static class Query {
        private final QueryType type;
        private final String symbol;
        private final String exchange;
        private final MarketDataStatus status;
        private final LocalDateTime olderThan;
        private final LocalDateTime updatedAfter;
        
        private Query(QueryType type, String symbol, String exchange, MarketDataStatus status,
                     LocalDateTime olderThan, LocalDateTime updatedAfter) {
            this.type = type;
            this.symbol = symbol;
            this.exchange = exchange;
            this.status = status;
            this.olderThan = olderThan;
            this.updatedAfter = updatedAfter;
        }
        
        public static Query bySymbolAndExchange(String symbol, String exchange) {
            return new Query(QueryType.BY_SYMBOL_AND_EXCHANGE, symbol, exchange, null, null, null);
        }
        
        public static Query bySymbol(String symbol) {
            return new Query(QueryType.BY_SYMBOL, symbol, null, null, null, null);
        }
        
        public static Query byExchange(String exchange) {
            return new Query(QueryType.BY_EXCHANGE, null, exchange, null, null, null);
        }
        
        public static Query byStatus(MarketDataStatus status) {
            return new Query(QueryType.BY_STATUS, null, null, status, null, null);
        }
        
        public static Query activeData() {
            return new Query(QueryType.ACTIVE_DATA, null, null, null, null, null);
        }
        
        public static Query staleData(LocalDateTime olderThan) {
            return new Query(QueryType.STALE_DATA, null, null, null, olderThan, null);
        }
        
        public static Query updatedAfter(LocalDateTime timestamp) {
            return new Query(QueryType.UPDATED_AFTER, null, null, null, null, timestamp);
        }
        
        // Getters
        public QueryType getType() { return type; }
        public String getSymbol() { return symbol; }
        public String getExchange() { return exchange; }
        public MarketDataStatus getStatus() { return status; }
        public LocalDateTime getOlderThan() { return olderThan; }
        public LocalDateTime getUpdatedAfter() { return updatedAfter; }
    }
    
    /**
     * Result для получения рыночных данных
     */
    public static class Result {
        private final List<MarketData> data;
        private final boolean isEmpty;
        
        private Result(List<MarketData> data) {
            this.data = data != null ? data : List.of();
            this.isEmpty = this.data.isEmpty();
        }
        
        public static Result single(MarketData data) {
            return new Result(List.of(data));
        }
        
        public static Result multiple(List<MarketData> data) {
            return new Result(data);
        }
        
        public static Result empty() {
            return new Result(List.of());
        }
        
        public List<MarketData> getData() { return data; }
        public boolean isEmpty() { return isEmpty; }
        public int getCount() { return data.size(); }
        
        public Optional<MarketData> getSingle() {
            return data.isEmpty() ? Optional.empty() : Optional.of(data.get(0));
        }
    }
    
    /**
     * Типы запросов
     */
    public enum QueryType {
        BY_SYMBOL_AND_EXCHANGE,
        BY_SYMBOL,
        BY_EXCHANGE,
        BY_STATUS,
        ACTIVE_DATA,
        STALE_DATA,
        UPDATED_AFTER
    }
} 
