package com.ct01.migration.domain;

import java.util.Objects;

/**
 * Правило валидации для проверки данных при миграции
 */
public class ValidationRule {
    
    private final String name;
    private final String description;
    private final String validationQuery;
    private final String expectedResult;
    private final ValidationRuleType type;
    private final boolean isRequired;
    
    public ValidationRule(String name, String description, String validationQuery, 
                         String expectedResult, ValidationRuleType type, boolean isRequired) {
        this.name = Objects.requireNonNull(name, "Название правила не может быть null");
        this.description = Objects.requireNonNull(description, "Описание правила не может быть null");
        this.validationQuery = Objects.requireNonNull(validationQuery, "SQL запрос не может быть null");
        this.expectedResult = expectedResult;
        this.type = Objects.requireNonNull(type, "Тип правила не может быть null");
        this.isRequired = isRequired;
        
        validateQuery();
    }
    
    /**
     * Создать правило проверки количества записей
     */
    public static ValidationRule recordCount(String name, String tableName, long expectedCount) {
        String query = String.format("SELECT COUNT(*) FROM %s", tableName);
        return new ValidationRule(
            name,
            String.format("Проверка количества записей в таблице %s", tableName),
            query,
            String.valueOf(expectedCount),
            ValidationRuleType.RECORD_COUNT,
            true
        );
    }
    
    /**
     * Создать правило проверки целостности данных
     */
    public static ValidationRule dataIntegrity(String name, String description, String query) {
        return new ValidationRule(
            name,
            description,
            query,
            "0", // Ожидаем 0 нарушений
            ValidationRuleType.DATA_INTEGRITY,
            true
        );
    }
    
    /**
     * Создать правило проверки уникальности
     */
    public static ValidationRule uniqueness(String name, String tableName, String columnName) {
        String query = String.format(
            "SELECT COUNT(*) - COUNT(DISTINCT %s) FROM %s WHERE %s IS NOT NULL",
            columnName, tableName, columnName
        );
        return new ValidationRule(
            name,
            String.format("Проверка уникальности %s в таблице %s", columnName, tableName),
            query,
            "0", // Ожидаем 0 дублей
            ValidationRuleType.UNIQUENESS,
            true
        );
    }
    
    /**
     * Создать правило проверки ссылочной целостности
     */
    public static ValidationRule referentialIntegrity(String name, String childTable, String childColumn,
                                                    String parentTable, String parentColumn) {
        String query = String.format(
            "SELECT COUNT(*) FROM %s c LEFT JOIN %s p ON c.%s = p.%s WHERE p.%s IS NULL AND c.%s IS NOT NULL",
            childTable, parentTable, childColumn, parentColumn, parentColumn, childColumn
        );
        return new ValidationRule(
            name,
            String.format("Проверка ссылочной целостности %s.%s -> %s.%s", 
                         childTable, childColumn, parentTable, parentColumn),
            query,
            "0", // Ожидаем 0 нарушений
            ValidationRuleType.REFERENTIAL_INTEGRITY,
            true
        );
    }
    
    /**
     * Проверить корректность SQL запроса
     */
    private void validateQuery() {
        if (validationQuery.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL запрос не может быть пустым");
        }
        
        String upperQuery = validationQuery.trim().toUpperCase();
        if (!upperQuery.startsWith("SELECT")) {
            throw new IllegalArgumentException("Запрос валидации должен быть SELECT");
        }
        
        // Проверяем на опасные операции
        if (upperQuery.contains("DELETE") || upperQuery.contains("UPDATE") || 
            upperQuery.contains("INSERT") || upperQuery.contains("DROP") ||
            upperQuery.contains("CREATE") || upperQuery.contains("ALTER")) {
            throw new IllegalArgumentException("Запрос валидации не должен изменять данные");
        }
    }
    
    /**
     * Проверить соответствие результата ожидаемому значению
     */
    public boolean isResultValid(String actualResult) {
        if (expectedResult == null) {
            return true; // Если ожидаемый результат не задан, любой результат валиден
        }
        
        return Objects.equals(expectedResult.trim(), actualResult != null ? actualResult.trim() : null);
    }
    
    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getValidationQuery() { return validationQuery; }
    public String getExpectedResult() { return expectedResult; }
    public ValidationRuleType getType() { return type; }
    public boolean isRequired() { return isRequired; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationRule that = (ValidationRule) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(validationQuery, that.validationQuery);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, validationQuery);
    }
    
    @Override
    public String toString() {
        return String.format("ValidationRule{name='%s', type=%s, required=%s}",
                           name, type, isRequired);
    }
    
    /**
     * Тип правила валидации
     */
    public enum ValidationRuleType {
        RECORD_COUNT("Количество записей"),
        DATA_INTEGRITY("Целостность данных"),
        UNIQUENESS("Уникальность"),
        REFERENTIAL_INTEGRITY("Ссылочная целостность"),
        CUSTOM("Пользовательское правило");
        
        private final String displayName;
        
        ValidationRuleType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
} 