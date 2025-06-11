package com.ct01.user.api.controller;

import com.ct01.user.api.dto.*;
import com.ct01.user.application.facade.UserApplicationFacade;
import com.ct01.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API контроллер для управления пользователями
 * Версия API: v1
 * Bounded Context: User
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management API v1", description = "API для управления пользователями и ролями")
public class UserManagementApiController {
    
    private final UserApplicationFacade userFacade;
    
    /**
     * Получить всех пользователей (только для администраторов)
     */
    @Operation(
        summary = "Получить всех пользователей",
        description = "Возвращает пагинированный список всех пользователей системы",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Список получен"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Доступ запрещен")
        }
    )
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<ApiUserDto>>> getAllUsers(
            Pageable pageable,
            HttpServletRequest request) {
        
        log.debug("Запрос всех пользователей с пагинацией: {}", pageable);
        
        Page<ApiUserDto> users = userFacade.getAllUsers(pageable);
        
        return ResponseEntity.ok(
            ApiResponse.success(users, 
                String.format("Получено %d пользователей", users.getNumberOfElements()))
                .withTraceId(getTraceId(request))
        );
    }
    
    /**
     * Получить пользователя по ID
     */
    @Operation(
        summary = "Получить пользователя по ID",
        description = "Возвращает информацию о пользователе по его ID",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Пользователь найден"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Доступ запрещен")
        }
    )
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<ApiUserDto>> getUserById(
            @Parameter(description = "ID пользователя", example = "1")
            @PathVariable Long userId,
            HttpServletRequest request) {
        
        log.debug("Запрос пользователя по ID: {}", userId);
        
        return userFacade.getUserById(userId)
            .map(user -> ResponseEntity.ok(
                ApiResponse.success(user, "Пользователь найден")
                    .withTraceId(getTraceId(request))
            ))
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Создать нового пользователя
     */
    @Operation(
        summary = "Создать нового пользователя",
        description = "Создать нового пользователя в системе",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Пользователь создан"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Доступ запрещен")
        }
    )
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ApiUserDto>> createUser(
            @Valid @RequestBody ApiCreateUserRequest createRequest,
            HttpServletRequest request) {
        
        log.info("Создание нового пользователя: {}", createRequest.email());
        
        try {
            ApiUserDto user = userFacade.createUser(createRequest);
            
            return ResponseEntity.ok(
                ApiResponse.success(user, "Пользователь успешно создан")
                    .withTraceId(getTraceId(request))
            );
        } catch (IllegalArgumentException e) {
            log.error("Ошибка создания пользователя: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("INVALID_DATA", e.getMessage()));
        }
    }
    
    /**
     * Обновить пользователя
     */
    @Operation(
        summary = "Обновить пользователя",
        description = "Обновить информацию о пользователе",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Пользователь обновлен"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Доступ запрещен")
        }
    )
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<ApiUserDto>> updateUser(
            @Parameter(description = "ID пользователя", example = "1")
            @PathVariable Long userId,
            @Valid @RequestBody ApiUpdateUserRequest updateRequest,
            HttpServletRequest request) {
        
        log.info("Обновление пользователя: {}", userId);
        
        return userFacade.updateUser(userId, updateRequest)
            .map(user -> ResponseEntity.ok(
                ApiResponse.success(user, "Пользователь успешно обновлен")
                    .withTraceId(getTraceId(request))
            ))
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Деактивировать пользователя
     */
    @Operation(
        summary = "Деактивировать пользователя",
        description = "Деактивировать пользователя в системе",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Пользователь деактивирован"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Доступ запрещен")
        }
    )
    @PostMapping("/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deactivateUser(
            @Parameter(description = "ID пользователя", example = "1")
            @PathVariable Long userId,
            HttpServletRequest request) {
        
        log.info("Деактивация пользователя: {}", userId);
        
        boolean deactivated = userFacade.deactivateUser(userId);
        
        if (deactivated) {
            return ResponseEntity.ok(
                ApiResponse.success("Пользователь деактивирован", "Пользователь успешно деактивирован")
                    .withTraceId(getTraceId(request))
            );
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Получить роли пользователя
     */
    @Operation(
        summary = "Получить роли пользователя",
        description = "Возвращает список ролей пользователя",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Роли получены"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Доступ запрещен")
        }
    )
    @GetMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<List<ApiRoleDto>>> getUserRoles(
            @Parameter(description = "ID пользователя", example = "1")
            @PathVariable Long userId,
            HttpServletRequest request) {
        
        log.debug("Запрос ролей пользователя: {}", userId);
        
        List<ApiRoleDto> roles = userFacade.getUserRoles(userId);
        
        return ResponseEntity.ok(
            ApiResponse.success(roles, 
                String.format("Получено %d ролей", roles.size()))
                .withTraceId(getTraceId(request))
        );
    }
    
    /**
     * Назначить роль пользователю
     */
    @Operation(
        summary = "Назначить роль пользователю",
        description = "Назначить роль пользователю",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Роль назначена"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Пользователь или роль не найдены"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Доступ запрещен")
        }
    )
    @PostMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> assignRole(
            @Parameter(description = "ID пользователя", example = "1")
            @PathVariable Long userId,
            @Parameter(description = "ID роли", example = "2")
            @PathVariable Long roleId,
            HttpServletRequest request) {
        
        log.info("Назначение роли {} пользователю {}", roleId, userId);
        
        boolean assigned = userFacade.assignRole(userId, roleId);
        
        if (assigned) {
            return ResponseEntity.ok(
                ApiResponse.success("Роль назначена", "Роль успешно назначена пользователю")
                    .withTraceId(getTraceId(request))
            );
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Снять роль с пользователя
     */
    @Operation(
        summary = "Снять роль с пользователя",
        description = "Снять роль с пользователя",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Роль снята"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Пользователь или роль не найдены"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Доступ запрещен")
        }
    )
    @DeleteMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> removeRole(
            @Parameter(description = "ID пользователя", example = "1")
            @PathVariable Long userId,
            @Parameter(description = "ID роли", example = "2")
            @PathVariable Long roleId,
            HttpServletRequest request) {
        
        log.info("Снятие роли {} с пользователя {}", roleId, userId);
        
        boolean removed = userFacade.removeRole(userId, roleId);
        
        if (removed) {
            return ResponseEntity.ok(
                ApiResponse.success("Роль снята", "Роль успешно снята с пользователя")
                    .withTraceId(getTraceId(request))
            );
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Получить все доступные роли
     */
    @Operation(
        summary = "Получить все роли",
        description = "Возвращает список всех доступных ролей в системе",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Роли получены"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Доступ запрещен")
        }
    )
    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ApiRoleDto>>> getAllRoles(
            HttpServletRequest request) {
        
        log.debug("Запрос всех ролей");
        
        List<ApiRoleDto> roles = userFacade.getAllRoles();
        
        return ResponseEntity.ok(
            ApiResponse.success(roles, 
                String.format("Получено %d ролей", roles.size()))
                .withTraceId(getTraceId(request))
        );
    }
    
    private String getTraceId(HttpServletRequest request) {
        return request.getHeader("X-Trace-ID");
    }
} 