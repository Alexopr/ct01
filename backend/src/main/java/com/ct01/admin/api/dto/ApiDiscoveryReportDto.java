package com.ct01.admin.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO для отчета обнаружения API
 */
@Schema(description = "Отчет об обнаружении API endpoints")
public record ApiDiscoveryReportDto(
    @Schema(description = "Время обнаружения")
    @JsonProperty("discoveryTimestamp")
    LocalDateTime discoveryTimestamp,
    
    @Schema(description = "Общее количество endpoints", example = "45")
    @JsonProperty("totalEndpoints")
    Integer totalEndpoints,
    
    @Schema(description = "Количество контроллеров", example = "8")
    @JsonProperty("controllerCount")
    Integer controllerCount,
    
    @Schema(description = "Список обнаруженных endpoints")
    @JsonProperty("endpoints")
    List<ApiEndpointInfoDto> endpoints,
    
    @Schema(description = "Сводная статистика")
    @JsonProperty("summary")
    ApiDiscoverySummaryDto summary
) {} 