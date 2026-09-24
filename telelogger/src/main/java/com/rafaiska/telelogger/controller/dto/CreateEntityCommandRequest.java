package com.rafaiska.telelogger.controller.dto;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateEntityCommandRequest(
        @NotNull @Positive Long session,
        @NotBlank @Size(max = 150) String entityId,
        Map<String, Object> entityState,
        @NotBlank @Size(max = 100) String commandType,
        @NotNull @PositiveOrZero Long timestampMs) {

    public CreateEntityCommandRequest {
        entityState = entityState == null ? new LinkedHashMap<>() : entityState;
    }
}
