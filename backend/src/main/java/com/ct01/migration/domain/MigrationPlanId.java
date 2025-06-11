package com.ct01.migration.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Идентификатор плана миграции
 */
public class MigrationPlanId {
    
    private final String value;
    
    public MigrationPlanId(String value) {
        this.value = Objects.requireNonNull(value, "Migration plan ID не может быть null");
        
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("Migration plan ID не может быть пустым");
        }
    }
    
    /**
     * Создать новый случайный ID
     */
    public static MigrationPlanId generate() {
        return new MigrationPlanId(UUID.randomUUID().toString());
    }
    
    /**
     * Создать ID из строки
     */
    public static MigrationPlanId of(String value) {
        return new MigrationPlanId(value);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MigrationPlanId that = (MigrationPlanId) o;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
} 