package com.ct01.core.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Базовый интерфейс для Aggregate Root в DDD.
 * Aggregate Root - это единственная точка входа в агрегат,
 * которая обеспечивает целостность и инварианты всего агрегата.
 * 
 * Принципы Aggregate Root:
 * - Единственная точка входа в агрегат
 * - Обеспечивает транзакционную целостность
 * - Публикует доменные события
 * - Контролирует доступ к внутренним сущностям
 */
public interface AggregateRoot<ID> extends Entity<ID> {
    
    /**
     * Получить список доменных событий, которые произошли в агрегате.
     * События должны быть опубликованы после успешного сохранения агрегата.
     * 
     * @return неизменяемый список доменных событий
     */
    default List<DomainEvent> getUncommittedEvents() {
        return Collections.emptyList();
    }
    
    /**
     * Очистить список доменных событий после их публикации.
     * Вызывается после успешного сохранения и публикации событий.
     */
    default void markEventsAsCommitted() {
        // Базовая реализация - переопределить в конкретных классах
    }
    
    /**
     * Добавить доменное событие к агрегату.
     * Событие будет опубликовано после сохранения агрегата.
     * 
     * @param event доменное событие
     */
    default void addEvent(DomainEvent event) {
        // Базовая реализация - переопределить в конкретных классах
    }
    
    /**
     * Проверить все бизнес-правила и инварианты агрегата.
     * Переопределяет базовый метод Entity для более строгой проверки.
     */
    @Override
    default void checkInvariants() {
        Entity.super.checkInvariants();
        // Дополнительные проверки агрегата можно добавить в конкретных классах
    }
} 
