package com.ct01.admin.application.usecase;

import com.ct01.admin.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case для выполнения административных операций
 */
@Service
@RequiredArgsConstructor
public class ExecuteAdminOperationUseCase {
    
    private final AdminRepository adminRepository;
    
    @Transactional
    public void execute(AdminId adminId) {
        Admin admin = adminRepository.findById(adminId)
            .orElseThrow(() -> new AdminException("Административная операция не найдена: " + adminId));
            
        admin.execute();
        adminRepository.save(admin);
    }
} 