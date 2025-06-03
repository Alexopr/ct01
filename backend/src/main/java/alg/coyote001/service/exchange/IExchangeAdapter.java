package alg.coyote001.service.exchange;

import alg.coyote001.dto.TickerData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Consumer;

/**
 * Унифицированный интерфейс для работы с биржами криптовалют
 */
public interface IExchangeAdapter {
    
    /**
     * Название биржи
     */
    String getExchangeName();
    
    /**
     * Инициализация подключения к бирже
     */
    Mono<Void> initialize();
    
    /**
     * Получить текущий тикер для указанной торговой пары
     * @param symbol торговая пара (например: BTC/USDT)
     * @return данные тикера
     */
    Mono<TickerData> fetchTicker(String symbol);
    
    /**
     * Получить тикеры для нескольких торговых пар
     * @param symbols список торговых пар
     * @return поток данных тикеров
     */
    Flux<TickerData> fetchTickers(List<String> symbols);
    
    /**
     * Подписаться на обновления тикера в реальном времени
     * @param symbol торговая пара
     * @param callback функция обратного вызова для получения данных
     * @return управление подпиской
     */
    Mono<Void> subscribeToTicker(String symbol, Consumer<TickerData> callback);
    
    /**
     * Отписаться от обновлений тикера
     * @param symbol торговая пара
     */
    Mono<Void> unsubscribeFromTicker(String symbol);
    
    /**
     * Проверить доступность биржи
     * @return true если биржа доступна
     */
    Mono<Boolean> isHealthy();
    
    /**
     * Закрыть соединение с биржей
     */
    Mono<Void> disconnect();
    
    /**
     * Получить информацию о лимитах API
     * @return информация о текущих лимитах
     */
    Mono<RateLimitInfo> getRateLimitInfo();
    
    /**
     * Получить список поддерживаемых торговых пар
     * @return список символов
     */
    Mono<List<String>> getSupportedSymbols();
} 