package com.ct01.websocket.api;

import com.ct01.websocket.infrastructure.migration.ParityTestResults;
import com.ct01.websocket.infrastructure.migration.ParityTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API для запуска тестов функциональной эквивалентности между системами
 */
@RestController
@RequestMapping("/api/websocket/testing")
public class TestingController {
    
    private static final Logger logger = LoggerFactory.getLogger(TestingController.class);
    
    private final ParityTestSuite parityTestSuite;
    
    public TestingController(ParityTestSuite parityTestSuite) {
        this.parityTestSuite = parityTestSuite;
    }
    
    /**
     * Запуск полного набора тестов сравнения систем
     */
    @PostMapping("/parity/full")
    public ResponseEntity<ParityTestResults> runFullParityTest() {
        logger.info("Starting full parity test suite via API");
        
        try {
            ParityTestResults results = parityTestSuite.runFullParityTest();
            
            logger.info("Parity test completed. Success rate: {:.1f}% ({}/{} tests passed)", 
                results.successPercentage(),
                results.passedCount(),
                results.testResults().size()
            );
            
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            logger.error("Failed to run parity test suite", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Получить статус готовности к миграции
     */
    @GetMapping("/migration-readiness")
    public ResponseEntity<MigrationReadinessStatus> getMigrationReadiness() {
        try {
            // Запускаем быстрый тест
            ParityTestResults results = parityTestSuite.runFullParityTest();
            
            boolean ready = results.successPercentage() >= 95.0; // 95% тестов должны проходить
            
            MigrationReadinessStatus status = new MigrationReadinessStatus(
                ready,
                results.successPercentage(),
                results.passedCount(),
                results.testResults().size(),
                ready ? "System ready for migration" : "System not ready - fix failing tests",
                results.timestamp()
            );
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            logger.error("Failed to check migration readiness", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

/**
 * Статус готовности к миграции
 */
record MigrationReadinessStatus(
    boolean ready,
    double successPercentage,
    long passedTests,
    long totalTests,
    String message,
    java.time.LocalDateTime timestamp
) {} 