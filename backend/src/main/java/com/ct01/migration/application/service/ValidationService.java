package com.ct01.migration.application.service;

import com.ct01.migration.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Сервис валидации данных для миграции
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ValidationService {
    
    private final JdbcTemplate jdbcTemplate;
    
    /**
     * Валидация перед выполнением плана миграции
     */
    public void validateBeforeExecution(MigrationPlan migrationPlan) {
        log.info("Валидация плана миграции перед выполнением: {}", migrationPlan.getId());
        
        List<String> validationErrors = new ArrayList<>();
        
        // Валидация каждого шага
        for (MigrationStep step : migrationPlan.getSteps()) {
            validateStep(step, validationErrors);
        }
        
        // Валидация целостности плана
        validatePlanIntegrity(migrationPlan, validationErrors);
        
        if (!validationErrors.isEmpty()) {
            String errorMessage = "Валидация не пройдена: " + String.join("; ", validationErrors);
            log.error(errorMessage);
            throw new ValidationException(errorMessage);
        }
        
        log.info("Валидация плана миграции успешно завершена");
    }
    
    /**
     * Валидация отдельного шага миграции
     */
    public void validateStep(MigrationStep step, List<String> errors) {
        log.debug("Валидация шага: {}", step.getName());
        
        switch (step.getType()) {
            case DATA_MIGRATION -> validateDataMigrationStep(step, errors);
            case SCHEMA_UPDATE -> validateSchemaUpdateStep(step, errors);
            case VALIDATION -> validateValidationStep(step, errors);
            case CLEANUP -> validateCleanupStep(step, errors);
            case INDEX_CREATION -> validateIndexCreationStep(step, errors);
            case CONFIG_UPDATE -> validateConfigUpdateStep(step, errors);
        }
    }
    
    /**
     * Валидация данных после миграции
     */
    public ValidationResult validateAfterMigration(MigrationStep step) {
        log.info("Валидация после выполнения шага: {}", step.getName());
        
        if (step.getValidationRule() == null) {
            return ValidationResult.success("Правила валидации не заданы");
        }
        
        try {
            return executeValidationRule(step.getValidationRule());
        } catch (Exception e) {
            log.error("Ошибка при валидации после миграции: {}", e.getMessage(), e);
            return ValidationResult.failure("Ошибка валидации: " + e.getMessage());
        }
    }
    
    private void validateDataMigrationStep(MigrationStep step, List<String> errors) {
        // Проверка существования исходной таблицы
        if (step.getSourceTable() != null && !tableExists(step.getSourceTable())) {
            errors.add("Исходная таблица не существует: " + step.getSourceTable());
        }
        
        // Проверка существования целевой таблицы
        if (step.getTargetTable() != null && !tableExists(step.getTargetTable())) {
            errors.add("Целевая таблица не существует: " + step.getTargetTable());
        }
        
        // Проверка скрипта трансформации
        if (step.getTransformationScript() == null || step.getTransformationScript().trim().isEmpty()) {
            errors.add("Скрипт трансформации не задан для шага: " + step.getName());
        }
    }
    
    private void validateSchemaUpdateStep(MigrationStep step, List<String> errors) {
        if (step.getTransformationScript() == null || step.getTransformationScript().trim().isEmpty()) {
            errors.add("Скрипт обновления схемы не задан для шага: " + step.getName());
        }
    }
    
    private void validateValidationStep(MigrationStep step, List<String> errors) {
        if (step.getValidationRule() == null) {
            errors.add("Правило валидации не задано для шага: " + step.getName());
        }
    }
    
    private void validateCleanupStep(MigrationStep step, List<String> errors) {
        if (step.getTargetTable() == null) {
            errors.add("Целевая таблица для очистки не указана: " + step.getName());
        }
    }
    
    private void validateIndexCreationStep(MigrationStep step, List<String> errors) {
        if (step.getTargetTable() == null) {
            errors.add("Таблица для создания индекса не указана: " + step.getName());
        }
    }
    
    private void validateConfigUpdateStep(MigrationStep step, List<String> errors) {
        if (step.getTransformationScript() == null || step.getTransformationScript().trim().isEmpty()) {
            errors.add("Скрипт обновления конфигурации не задан для шага: " + step.getName());
        }
    }
    
    private void validatePlanIntegrity(MigrationPlan migrationPlan, List<String> errors) {
        // Проверка уникальности порядковых номеров шагов
        List<Integer> orders = migrationPlan.getSteps().stream()
                                          .map(MigrationStep::getOrder)
                                          .toList();
        
        long distinctOrders = orders.stream().distinct().count();
        if (distinctOrders != orders.size()) {
            errors.add("Обнаружены дублирующиеся порядковые номера шагов");
        }
        
        // Проверка последовательности порядковых номеров
        List<Integer> sortedOrders = orders.stream().sorted().toList();
        for (int i = 0; i < sortedOrders.size(); i++) {
            if (sortedOrders.get(i) != i + 1) {
                errors.add("Нарушена последовательность порядковых номеров шагов");
                break;
            }
        }
    }
    
    private boolean tableExists(String tableName) {
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName);
            return count != null && count > 0;
        } catch (Exception e) {
            log.warn("Ошибка при проверке существования таблицы {}: {}", tableName, e.getMessage());
            return false;
        }
    }
    
    private ValidationResult executeValidationRule(ValidationRule rule) {
        try {
            Object result = jdbcTemplate.queryForObject(rule.getValidationQuery(), Object.class);
            String actualResult = result != null ? result.toString() : "null";
            
            boolean isValid = rule.getExpectedResult().equals(actualResult);
            
            if (isValid) {
                return ValidationResult.success("Валидация успешна: " + rule.getName());
            } else {
                return ValidationResult.failure(
                    String.format("Валидация не пройдена: %s. Ожидалось: %s, получено: %s", 
                                rule.getName(), rule.getExpectedResult(), actualResult)
                );
            }
        } catch (Exception e) {
            return ValidationResult.failure("Ошибка выполнения валидации: " + e.getMessage());
        }
    }
    
    /**
     * Результат валидации
     */
    public static class ValidationResult {
        private final boolean success;
        private final String message;
        
        private ValidationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public static ValidationResult success(String message) {
            return new ValidationResult(true, message);
        }
        
        public static ValidationResult failure(String message) {
            return new ValidationResult(false, message);
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
    
    /**
     * Исключение валидации
     */
    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }
} 