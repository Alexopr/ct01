package com.ct01.crypto.application.usecase;

import com.ct01.crypto.application.dto.CoinQuery;
import com.ct01.crypto.application.dto.CoinResult;
import com.ct01.crypto.domain.Coin;
import com.ct01.crypto.domain.TrackedCoin;
import com.ct01.crypto.domain.repository.CoinRepository;
import com.ct01.crypto.domain.repository.TrackedCoinRepository;
import com.ct01.crypto.domain.service.CoinDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Use Case для получения данных о монетах
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GetCoinDataUseCase {
    
    private final CoinRepository coinRepository;
    private final TrackedCoinRepository trackedCoinRepository;
    private final CoinDomainService coinDomainService;
    
    /**
     * Получить монету по символу
     */
    public Optional<Coin> getCoin(CoinQuery.GetCoinQuery query) {
        log.debug("Getting coin by symbol: {}", query.symbol());
        return coinRepository.findBySymbol(query.symbol());
    }
    
    /**
     * Получить отслеживаемую монету
     */
    public Optional<TrackedCoin> getTrackedCoin(CoinQuery.GetTrackedCoinQuery query) {
        log.debug("Getting tracked coin by ID: {} or symbol: {}", query.id(), query.symbol());
        
        if (query.id() != null) {
            return trackedCoinRepository.findById(query.id());
        } else {
            return trackedCoinRepository.findBySymbol(query.symbol());
        }
    }
    
    /**
     * Получить все активные монеты
     */
    public CoinResult.TrackedCoinListResult getActiveCoins() {
        log.debug("Getting all active coins");
        
        List<TrackedCoin> activeCoins = trackedCoinRepository.findActiveCoins();
        
        return new CoinResult.TrackedCoinListResult(
            activeCoins,
            activeCoins.size(),
            false,
            null
        );
    }
    
    /**
     * Получить все отслеживаемые монеты с пагинацией
     */
    public CoinResult.TrackedCoinListResult getAllTrackedCoins(Integer pageNumber, Integer pageSize) {
        log.debug("Getting all tracked coins with pagination: page {}, size {}", pageNumber, pageSize);
        
        int page = pageNumber != null ? pageNumber : 0;
        int size = pageSize != null ? pageSize : 20;
        
        Pageable pageable = PageRequest.of(page, size);
        Page<TrackedCoin> coinPage = trackedCoinRepository.findAll(pageable);
        
        return new CoinResult.TrackedCoinListResult(
            coinPage.getContent(),
            (int) coinPage.getTotalElements(),
            coinPage.hasNext(),
            coinPage.hasNext() ? String.valueOf(page + 1) : null
        );
    }
    
    /**
     * Поиск монет
     */
    public CoinResult.CoinSearchResult searchCoins(String searchTerm) {
        log.debug("Searching coins with term: {}", searchTerm);
        
        List<Coin> searchResults = coinDomainService.searchCoins(searchTerm);
        
        List<Coin> exactMatches = searchResults.stream()
            .filter(coin -> coin.getSymbol().equalsIgnoreCase(searchTerm) || 
                           coin.getName().equalsIgnoreCase(searchTerm))
            .toList();
        
        List<Coin> partialMatches = searchResults.stream()
            .filter(coin -> !exactMatches.contains(coin))
            .toList();
        
        return new CoinResult.CoinSearchResult(
            exactMatches,
            partialMatches,
            searchTerm,
            searchResults.size(),
            LocalDateTime.now()
        );
    }
} 
