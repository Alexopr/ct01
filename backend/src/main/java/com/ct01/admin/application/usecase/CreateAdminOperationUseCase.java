package com.ct01.admin.application.usecase;

import com.ct01.admin.domain.*;
import com.ct01.core.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Use Case для создания административных операций
 */
@Service
@RequiredArgsConstructor
public class CreateAdminOperationUseCase {
    
    private final AdminRepository adminRepository;
    
    @Transactional
    public AdminId execute(
            UserId userId, 
            AdminActionType actionType, 
            Map<String, Object> actionData,
            AdminPermissionLevel permissionLevel,
            String ipAddress,
            String userAgent) {
        
        AdminAction action = AdminAction.builder()
            .actionType(actionType)
            .actionData(actionData)
            .build();
            
        Admin admin = Admin.builder()
            .id(AdminId.nextId())
            .userId(userId)
            .action(action)
            .actionData(actionData)
            .permissionLevel(permissionLevel)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .build();
            
        adminRepository.save(admin);
        return admin.getId();
    }
} 