package com.ct01.admin.application.usecase;

import com.ct01.admin.domain.*;
import com.ct01.admin.domain.service.AdminDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use Case для обработки ожидающих административных операций
 */
@Service
@RequiredArgsConstructor
public class ProcessPendingOperationsUseCase {
    
    private final AdminRepository adminRepository;
    private final AdminDomainService adminDomainService;
    
    @Transactional
    public void execute() {
        List<Admin> pending = adminRepository.findByStatus(AdminStatus.PENDING, 0, 100);
        
        for (Admin admin : pending) {
            if (adminDomainService.canExecuteOperation(admin)) {
                admin.execute();
                adminRepository.save(admin);
            }
        }
    }
} 