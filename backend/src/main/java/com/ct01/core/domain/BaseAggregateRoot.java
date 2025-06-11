package com.ct01.core.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Базовый абстрактный класс для Aggregate Root в DDD
 */
public abstract class BaseAggregateRoot<ID> implements AggregateRoot<ID> {
    
    private ID id;
    private transient List<DomainEvent> uncommittedEvents = new ArrayList<>();
    
    protected BaseAggregateRoot(ID id) {
        this.id = id;
    }
    
    @Override
    public ID getId() {
        return id;
    }
    
    @Override
    public List<DomainEvent> getUncommittedEvents() {
        return Collections.unmodifiableList(uncommittedEvents);
    }
    
    @Override
    public void markEventsAsCommitted() {
        uncommittedEvents.clear();
    }
    
    @Override
    public void addEvent(DomainEvent event) {
        if (event != null) {
            uncommittedEvents.add(event);
        }
    }
    
    /**
     * Добавить доменное событие (алиас для addEvent для совместимости)
     */
    protected void addDomainEvent(DomainEvent event) {
        addEvent(event);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseAggregateRoot<?> that = (BaseAggregateRoot<?>) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
} 