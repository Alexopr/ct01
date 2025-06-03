package alg.coyote001.service;

import alg.coyote001.dto.CoinDto;
import alg.coyote001.entity.Coin;
import alg.coyote001.entity.Exchange;
import alg.coyote001.repository.CoinRepository;
import alg.coyote001.repository.ExchangeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for basic coin operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CoinService {
    
    private final CoinRepository coinRepository;
    private final ExchangeRepository exchangeRepository;
    
    /**
     * Get all coins with pagination
     */
    @Transactional(readOnly = true)
    public Page<CoinDto> getAllCoins(Pageable pageable) {
        log.debug("Fetching all coins with pagination: {}", pageable);
        
        return coinRepository.findAll(pageable)
                .map(this::convertToDto);
    }
    
    /**
     * Get active coins only
     */
    @Transactional(readOnly = true)
    public List<CoinDto> getActiveCoins() {
        log.debug("Fetching active coins");
        
        return coinRepository.findByIsActiveTrue()
                .stream()
                .map(this::convertToDto)
                .toList();
    }
    
    /**
     * Get coin by symbol
     */
    @Transactional(readOnly = true)
    public Optional<CoinDto> getCoinBySymbol(String symbol) {
        log.debug("Fetching coin by symbol: {}", symbol);
        
        return coinRepository.findBySymbolIgnoreCase(symbol)
                .map(this::convertToDto);
    }
    
    /**
     * Get coins by exchange
     */
    @Transactional(readOnly = true)
    public List<CoinDto> getCoinsByExchange(String exchangeName) {
        log.debug("Fetching coins for exchange: {}", exchangeName);
        
        Exchange exchange = exchangeRepository.findByNameIgnoreCase(exchangeName)
                .orElseThrow(() -> new IllegalArgumentException("Exchange not found: " + exchangeName));
        
        return coinRepository.findCoinsByExchange(exchange.getId())
                .stream()
                .map(this::convertToDto)
                .toList();
    }
    
    /**
     * Search coins by symbol pattern
     */
    @Transactional(readOnly = true)
    public List<CoinDto> searchCoinsBySymbol(String query) {
        log.debug("Searching coins with query: {}", query);
        
        return coinRepository.findBySymbolContainingIgnoreCaseAndIsActiveTrue(query)
                .stream()
                .map(this::convertToDto)
                .toList();
    }
    
    /**
     * Convert Coin entity to DTO
     */
    private CoinDto convertToDto(Coin coin) {
        return CoinDto.builder()
                .id(coin.getId())
                .symbol(coin.getSymbol())
                .name(coin.getName())
                .isActive(coin.getIsActive())
                .createdAt(coin.getCreatedAt())
                .updatedAt(coin.getUpdatedAt())
                .build();
    }
} 