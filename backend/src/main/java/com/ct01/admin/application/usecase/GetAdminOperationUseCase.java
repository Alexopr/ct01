package com.ct01.admin.application.usecase;

import com.ct01.admin.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Use Case для получения одной административной операции
 */
@Service
@RequiredArgsConstructor
public class GetAdminOperationUseCase {
    
    private final AdminRepository adminRepository;
    
    @Transactional(readOnly = true)
    public Optional<Admin> execute(AdminId adminId) {
        return adminRepository.findById(adminId);
    }
} 