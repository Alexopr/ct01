package com.ct01.system.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO для системной информации
 */
@Schema(description = "Информация о системе")
public record ApiSystemInfoDto(
    @Schema(description = "Название системы", example = "CT.01")
    @JsonProperty("systemName")
    String systemName,
    
    @Schema(description = "Версия системы", example = "1.0.0")
    @JsonProperty("version")
    String version,
    
    @Schema(description = "Профиль окружения", example = "production")
    @JsonProperty("environment")
    String environment,
    
    @Schema(description = "Время запуска системы")
    @JsonProperty("startupTime")
    LocalDateTime startupTime,
    
    @Schema(description = "Время работы в секундах", example = "3600")
    @JsonProperty("uptimeSeconds")
    Long uptimeSeconds,
    
    @Schema(description = "Статус системы", example = "HEALTHY")
    @JsonProperty("status")
    String status,
    
    @Schema(description = "JVM информация")
    @JsonProperty("jvmInfo")
    Map<String, Object> jvmInfo,
    
    @Schema(description = "Дополнительная информация")
    @JsonProperty("additionalInfo")
    Map<String, Object> additionalInfo
) {} 