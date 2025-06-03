package alg.coyote001.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Унифицированная структура данных тикера для всех бирж
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TickerData {
    
    /**
     * Символ торговой пары (например: BTC/USDT)
     */
    private String symbol;
    
    /**
     * Название биржи
     */
    private String exchange;
    
    /**
     * Текущая цена
     */
    private BigDecimal price;
    
    /**
     * Цена покупки (bid)
     */
    private BigDecimal bid;
    
    /**
     * Цена продажи (ask)
     */
    private BigDecimal ask;
    
    /**
     * Объем торгов за 24 часа
     */
    private BigDecimal volume24h;
    
    /**
     * Изменение цены за 24 часа в процентах
     */
    private BigDecimal change24h;
    
    /**
     * Временная метка последнего обновления
     */
    private LocalDateTime timestamp;
    
    /**
     * Статус данных (ACTIVE, STALE, ERROR)
     */
    private TickerStatus status;
    
    /**
     * Сообщение об ошибке (если status = ERROR)
     */
    private String error;
    
    public enum TickerStatus {
        ACTIVE,    // Данные актуальные
        STALE,     // Данные устарели
        ERROR      // Ошибка получения данных
    }
} 