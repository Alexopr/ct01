package com.ct01.admin.application.usecase;

import com.ct01.admin.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Use Case для очистки истекших административных операций
 */
@Service
@RequiredArgsConstructor
public class CleanupExpiredOperationsUseCase {
    
    private final AdminRepository adminRepository;
    
    @Transactional
    public void execute() {
        List<Admin> expired = adminRepository.findExpiredOperations(LocalDateTime.now());
        
        for (Admin admin : expired) {
            admin.fail("Операция истекла");
            adminRepository.save(admin);
        }
    }
} 