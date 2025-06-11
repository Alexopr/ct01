package com.ct01.migration.api;

import com.ct01.migration.application.command.*;
import com.ct01.migration.application.service.MetricsService;
import com.ct01.migration.application.usecase.*;
import com.ct01.migration.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API контроллер для управления планами миграции
 */
@RestController
@RequestMapping("/api/migration")
@RequiredArgsConstructor
@Slf4j
public class MigrationPlanController {
    
    private final CreateMigrationPlanUseCase createMigrationPlanUseCase;
    private final ExecuteMigrationPlanUseCase executeMigrationPlanUseCase;
    private final MigrationPlanRepository migrationPlanRepository;
    private final MetricsService metricsService;
    
    /**
     * Получить все планы миграции
     */
    @GetMapping("/plans")
    public ResponseEntity<List<MigrationPlan>> getAllPlans() {
        log.info("Запрос всех планов миграции");
        List<MigrationPlan> plans = migrationPlanRepository.findAll();
        return ResponseEntity.ok(plans);
    }
    
    /**
     * Получить план миграции по ID
     */
    @GetMapping("/plans/{id}")
    public ResponseEntity<MigrationPlan> getPlan(@PathVariable String id) {
        log.info("Запрос плана миграции: {}", id);
        
        MigrationPlanId planId = MigrationPlanId.of(id);
        return migrationPlanRepository.findById(planId)
                .map(plan -> ResponseEntity.ok(plan))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Создать новый план миграции
     */
    @PostMapping("/plans")
    public ResponseEntity<MigrationPlan> createPlan(@RequestBody CreateMigrationPlanRequest request) {
        log.info("Создание нового плана миграции: {}", request.getName());
        
        try {
            CreateMigrationPlanCommand command = new CreateMigrationPlanCommand(
                request.getName(),
                request.getDescription(),
                request.getStrategy(),
                request.getSteps()
            );
            
            MigrationPlan plan = createMigrationPlanUseCase.execute(command);
            return ResponseEntity.status(HttpStatus.CREATED).body(plan);
            
        } catch (Exception e) {
            log.error("Ошибка при создании плана миграции: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Выполнить план миграции
     */
    @PostMapping("/plans/{id}/execute")
    public ResponseEntity<String> executePlan(@PathVariable String id, 
                                            @RequestParam(defaultValue = "false") boolean dryRun,
                                            @RequestParam(defaultValue = "true") boolean preValidation) {
        log.info("Запрос выполнения плана миграции: {} (dryRun: {})", id, dryRun);
        
        try {
            MigrationPlanId planId = MigrationPlanId.of(id);
            ExecuteMigrationPlanCommand command = new ExecuteMigrationPlanCommand(
                planId, dryRun, preValidation, false
            );
            
            executeMigrationPlanUseCase.execute(command);
            
            String message = dryRun ? "Тестовое выполнение запущено" : "Выполнение плана миграции запущено";
            return ResponseEntity.ok(message);
            
        } catch (Exception e) {
            log.error("Ошибка при выполнении плана миграции: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Ошибка: " + e.getMessage());
        }
    }
    
    /**
     * Получить планы миграции по статусу
     */
    @GetMapping("/plans/status/{status}")
    public ResponseEntity<List<MigrationPlan>> getPlansByStatus(@PathVariable String status) {
        log.info("Запрос планов миграции по статусу: {}", status);
        
        try {
            MigrationStatus migrationStatus = MigrationStatus.valueOf(status.toUpperCase());
            List<MigrationPlan> plans = migrationPlanRepository.findByStatus(migrationStatus);
            return ResponseEntity.ok(plans);
        } catch (IllegalArgumentException e) {
            log.warn("Неверный статус миграции: {}", status);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Получить активные планы миграции
     */
    @GetMapping("/plans/active")
    public ResponseEntity<List<MigrationPlan>> getActivePlans() {
        log.info("Запрос активных планов миграции");
        List<MigrationPlan> activePlans = migrationPlanRepository.findActivePlans();
        return ResponseEntity.ok(activePlans);
    }
    
    /**
     * Получить планы миграции готовые к выполнению
     */
    @GetMapping("/plans/ready")
    public ResponseEntity<List<MigrationPlan>> getReadyPlans() {
        log.info("Запрос планов миграции готовых к выполнению");
        List<MigrationPlan> readyPlans = migrationPlanRepository.findReadyToExecute();
        return ResponseEntity.ok(readyPlans);
    }
    
    /**
     * Получить сводку выполнения плана миграции
     */
    @GetMapping("/plans/{id}/summary")
    public ResponseEntity<MetricsService.ExecutionSummary> getPlanSummary(@PathVariable String id) {
        log.info("Запрос сводки плана миграции: {}", id);
        
        MigrationPlanId planId = MigrationPlanId.of(id);
        return migrationPlanRepository.findById(planId)
                .map(plan -> {
                    MetricsService.ExecutionSummary summary = metricsService.getExecutionSummary(plan);
                    return ResponseEntity.ok(summary);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Получить метрики шагов плана миграции
     */
    @GetMapping("/plans/{id}/metrics")
    public ResponseEntity<List<MetricsService.StepMetrics>> getPlanMetrics(@PathVariable String id) {
        log.info("Запрос метрик плана миграции: {}", id);
        
        MigrationPlanId planId = MigrationPlanId.of(id);
        return migrationPlanRepository.findById(planId)
                .map(plan -> {
                    List<MetricsService.StepMetrics> metrics = plan.getSteps().stream()
                            .map(metricsService::calculateStepMetrics)
                            .toList();
                    return ResponseEntity.ok(metrics);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Удалить план миграции
     */
    @DeleteMapping("/plans/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable String id) {
        log.info("Запрос удаления плана миграции: {}", id);
        
        MigrationPlanId planId = MigrationPlanId.of(id);
        return migrationPlanRepository.findById(planId)
                .map(plan -> {
                    if (plan.getStatus() == MigrationStatus.RUNNING || plan.getStatus() == MigrationStatus.ROLLBACK) {
                        log.warn("Попытка удаления активного плана миграции: {}", id);
                        return ResponseEntity.status(HttpStatus.CONFLICT).<Void>build();
                    }
                    
                    migrationPlanRepository.delete(plan);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Получить статистику планов миграции
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("Запрос статистики планов миграции");
        
        Map<String, Object> stats = Map.of(
            "totalPlans", migrationPlanRepository.count(),
            "activePlans", migrationPlanRepository.findActivePlans().size(),
            "statusBreakdown", migrationPlanRepository.findAll().stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                        plan -> plan.getStatus().toString(),
                        java.util.stream.Collectors.counting()
                    ))
        );
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * DTO для запроса создания плана миграции
     */
    public static class CreateMigrationPlanRequest {
        private String name;
        private String description;
        private MigrationStrategy strategy;
        private List<MigrationStepDefinition> steps;
        
        // Constructors
        public CreateMigrationPlanRequest() {}
        
        public CreateMigrationPlanRequest(String name, String description, MigrationStrategy strategy,
                                        List<MigrationStepDefinition> steps) {
            this.name = name;
            this.description = description;
            this.strategy = strategy;
            this.steps = steps;
        }
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public MigrationStrategy getStrategy() { return strategy; }
        public void setStrategy(MigrationStrategy strategy) { this.strategy = strategy; }
        
        public List<MigrationStepDefinition> getSteps() { return steps; }
        public void setSteps(List<MigrationStepDefinition> steps) { this.steps = steps; }
    }
} 