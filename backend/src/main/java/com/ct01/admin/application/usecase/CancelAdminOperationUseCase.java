package com.ct01.admin.application.usecase;

import com.ct01.admin.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case для отмены административной операции
 */
@Service
@RequiredArgsConstructor
public class CancelAdminOperationUseCase {
    
    private final AdminRepository adminRepository;
    
    @Transactional
    public void execute(AdminId adminId, String reason) {
        Admin admin = adminRepository.findById(adminId)
            .orElseThrow(() -> new AdminException("Операция не найдена: " + adminId));
            
        admin.reject(reason);
        adminRepository.save(admin);
    }
} 