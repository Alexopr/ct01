package com.ct01.admin.application.usecase;

import com.ct01.admin.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Use Case для получения статистики административных операций
 */
@Service
@RequiredArgsConstructor
public class GetAdminOperationStatisticsUseCase {
    
    private final AdminRepository adminRepository;
    
    @Transactional(readOnly = true)
    public AdminOperationStatistics execute(LocalDateTime from, LocalDateTime to) {
        return adminRepository.getOperationStatistics(from, to);
    }
} 