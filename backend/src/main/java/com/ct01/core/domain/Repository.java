package com.ct01.core.domain;

import java.util.Optional;

/**
 * Базовый интерфейс для всех репозиториев в DDD.
 * Репозиторий инкапсулирует логику доступа к агрегатам,
 * предоставляя интерфейс коллекции для работы с доменными объектами.
 * 
 * Принципы Repository:
 * - Интерфейс находится в domain layer
 * - Реализация в infrastructure layer
 * - Работает только с агрегатными корнями
 * - Скрывает детали персистентности от домена
 * 
 * @param <T> тип агрегатного корня
 * @param <ID> тип идентификатора
 */
public interface Repository<T extends AggregateRoot<ID>, ID> {
    
    /**
     * Найти агрегат по идентификатору
     * @param id идентификатор агрегата
     * @return агрегат, если найден
     */
    Optional<T> findById(ID id);
    
    /**
     * Сохранить агрегат
     * @param aggregate агрегат для сохранения
     * @return сохраненный агрегат
     */
    T save(T aggregate);
    
    /**
     * Удалить агрегат
     * @param aggregate агрегат для удаления
     */
    void delete(T aggregate);
    
    /**
     * Удалить агрегат по идентификатору
     * @param id идентификатор агрегата
     */
    void deleteById(ID id);
    
    /**
     * Проверить существование агрегата по идентификатору
     * @param id идентификатор агрегата
     * @return true, если агрегат существует
     */
    boolean existsById(ID id);
    
    /**
     * Получить следующий доступный идентификатор
     * @return новый уникальный идентификатор
     */
    ID nextId();
} 
