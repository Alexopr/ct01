package com.ct01.subscription.domain;

import com.ct01.core.domain.ValueObject;

import java.time.LocalDateTime;
import java.time.Period;

/**
 * Value Object для периода биллинга
 */
public record BillingPeriod(Period period, String displayName) implements ValueObject {
    
    public BillingPeriod {
        if (period == null) {
            throw new IllegalArgumentException("Period не может быть null");
        }
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new IllegalArgumentException("DisplayName не может быть пустым");
        }
    }
    
    public static final BillingPeriod MONTHLY = new BillingPeriod(Period.ofMonths(1), "Ежемесячно");
    public static final BillingPeriod YEARLY = new BillingPeriod(Period.ofYears(1), "Ежегодно");
    public static final BillingPeriod WEEKLY = new BillingPeriod(Period.ofWeeks(1), "Еженедельно");
    
    /**
     * Вычисляет следующую дату биллинга
     */
    public LocalDateTime calculateNextBillingDate(LocalDateTime currentDate) {
        return currentDate.plus(period);
    }
    
    /**
     * Проверяет, является ли период месячным
     */
    public boolean isMonthly() {
        return period.equals(Period.ofMonths(1));
    }
    
    /**
     * Проверяет, является ли период годовым
     */
    public boolean isYearly() {
        return period.equals(Period.ofYears(1));
    }
    
    /**
     * Получает количество месяцев в периоде
     */
    public int getMonths() {
        return period.getMonths() + (period.getYears() * 12);
    }
    
    public static BillingPeriod fromString(String billingCycle) {
        return switch (billingCycle.toLowerCase()) {
            case "monthly", "month" -> MONTHLY;
            case "yearly", "year", "annual" -> YEARLY;
            case "weekly", "week" -> WEEKLY;
            default -> throw new IllegalArgumentException("Неизвестный период биллинга: " + billingCycle);
        };
    }
} 
