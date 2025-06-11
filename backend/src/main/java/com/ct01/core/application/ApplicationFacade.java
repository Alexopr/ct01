package com.ct01.core.application;

/**
 * Базовый интерфейс для Application Facade.
 * Application Facade предоставляет упрощенный интерфейс для клиентов,
 * скрывая сложность взаимодействия с множественными Use Cases и доменными сервисами.
 * 
 * Принципы Application Facade:
 * - Упрощенный интерфейс для внешних клиентов
 * - Оркестрация нескольких Use Cases
 * - Управление транзакциями
 * - Преобразование между внешними DTO и внутренними объектами
 * - Координация между различными bounded contexts
 */
public interface ApplicationFacade {
    
    /**
     * Получить название домена, который обслуживает данный фасад
     * @return название домена
     */
    default String getDomainName() {
        return this.getClass().getSimpleName().replace("ApplicationFacade", "").toLowerCase();
    }
} 
