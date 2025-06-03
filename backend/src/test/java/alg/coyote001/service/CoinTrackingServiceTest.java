package alg.coyote001.service;

import alg.coyote001.dto.TrackedCoinDto;
import alg.coyote001.entity.TrackedCoin;
import alg.coyote001.repository.TrackedCoinRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for CoinTrackingService
 */
@ExtendWith(MockitoExtension.class)
class CoinTrackingServiceTest {

    @Mock
    private TrackedCoinRepository trackedCoinRepository;

    @InjectMocks
    private CoinTrackingService coinTrackingService;

    private TrackedCoinDto testCoinDto;
    private TrackedCoin testCoinEntity;

    @BeforeEach
    void setUp() {
        testCoinDto = TrackedCoinDto.builder()
                .symbol("BTC")
                .name("Bitcoin")
                .exchanges(Set.of(TrackedCoin.Exchange.BINANCE, TrackedCoin.Exchange.BYBIT))
                .quoteCurrencies(Set.of("USDT", "USDC"))
                .isActive(true)
                .websocketEnabled(true)
                .priority(10)
                .notes("Test coin")
                .build();

        testCoinEntity = TrackedCoin.builder()
                .id(1L)
                .symbol("BTC")
                .name("Bitcoin")
                .exchanges(Set.of(TrackedCoin.Exchange.BINANCE, TrackedCoin.Exchange.BYBIT))
                .quoteCurrencies(Set.of("USDT", "USDC"))
                .isActive(true)
                .websocketEnabled(true)
                .priority(10)
                .notes("Test coin")
                .build();
    }

    @Test
    void testCreateTrackedCoin_Success() {
        // Given
        when(trackedCoinRepository.existsBySymbolIgnoreCase("BTC")).thenReturn(false);
        when(trackedCoinRepository.save(any(TrackedCoin.class))).thenReturn(testCoinEntity);

        // When
        TrackedCoinDto result = coinTrackingService.createTrackedCoin(testCoinDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSymbol()).isEqualTo("BTC");
        assertThat(result.getName()).isEqualTo("Bitcoin");
        verify(trackedCoinRepository).existsBySymbolIgnoreCase("BTC");
        verify(trackedCoinRepository).save(any(TrackedCoin.class));
    }

    @Test
    void testCreateTrackedCoin_DuplicateSymbol() {
        // Given
        when(trackedCoinRepository.existsBySymbolIgnoreCase("BTC")).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            coinTrackingService.createTrackedCoin(testCoinDto);
        });

        verify(trackedCoinRepository).existsBySymbolIgnoreCase("BTC");
        verify(trackedCoinRepository, never()).save(any(TrackedCoin.class));
    }

    @Test
    void testGetTrackedCoin_Found() {
        // Given
        when(trackedCoinRepository.findById(1L)).thenReturn(Optional.of(testCoinEntity));

        // When
        Optional<TrackedCoinDto> result = coinTrackingService.getTrackedCoin(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getSymbol()).isEqualTo("BTC");
        verify(trackedCoinRepository).findById(1L);
    }

    @Test
    void testGetTrackedCoin_NotFound() {
        // Given
        when(trackedCoinRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<TrackedCoinDto> result = coinTrackingService.getTrackedCoin(999L);

        // Then
        assertThat(result).isEmpty();
        verify(trackedCoinRepository).findById(999L);
    }

    @Test
    void testGetTrackedCoinBySymbol_Found() {
        // Given
        when(trackedCoinRepository.findBySymbolIgnoreCase("btc")).thenReturn(Optional.of(testCoinEntity));

        // When
        Optional<TrackedCoinDto> result = coinTrackingService.getTrackedCoinBySymbol("btc");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getSymbol()).isEqualTo("BTC");
        verify(trackedCoinRepository).findBySymbolIgnoreCase("btc");
    }

    @Test
    void testDeleteTrackedCoin_Success() {
        // Given
        when(trackedCoinRepository.existsById(1L)).thenReturn(true);

        // When
        coinTrackingService.deleteTrackedCoin(1L);

        // Then
        verify(trackedCoinRepository).existsById(1L);
        verify(trackedCoinRepository).deleteById(1L);
    }

    @Test
    void testDeleteTrackedCoin_NotFound() {
        // Given
        when(trackedCoinRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            coinTrackingService.deleteTrackedCoin(999L);
        });

        verify(trackedCoinRepository).existsById(999L);
        verify(trackedCoinRepository, never()).deleteById(anyLong());
    }

    @Test
    void testToggleActivation_Success() {
        // Given
        when(trackedCoinRepository.findById(1L)).thenReturn(Optional.of(testCoinEntity));
        when(trackedCoinRepository.save(any(TrackedCoin.class))).thenReturn(testCoinEntity);

        // When
        TrackedCoinDto result = coinTrackingService.toggleActivation(1L, false);

        // Then
        assertThat(result).isNotNull();
        verify(trackedCoinRepository).findById(1L);
        verify(trackedCoinRepository).save(any(TrackedCoin.class));
    }

    @Test
    void testToggleActivation_NotFound() {
        // Given
        when(trackedCoinRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            coinTrackingService.toggleActivation(999L, false);
        });

        verify(trackedCoinRepository).findById(999L);
        verify(trackedCoinRepository, never()).save(any(TrackedCoin.class));
    }

    @Test
    void testGetTrackingStats() {
        // Given
        when(trackedCoinRepository.countActiveCoins()).thenReturn(10L);
        when(trackedCoinRepository.countByExchange(TrackedCoin.Exchange.BINANCE)).thenReturn(8L);
        when(trackedCoinRepository.countByExchange(TrackedCoin.Exchange.BYBIT)).thenReturn(6L);
        when(trackedCoinRepository.countByExchange(TrackedCoin.Exchange.OKX)).thenReturn(4L);

        // When
        CoinTrackingService.TrackingStats stats = coinTrackingService.getTrackingStats();

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.getTotalActiveCoins()).isEqualTo(10L);
        assertThat(stats.getBinanceCoins()).isEqualTo(8L);
        assertThat(stats.getBybitCoins()).isEqualTo(6L);
        assertThat(stats.getOkxCoins()).isEqualTo(4L);
    }
} 