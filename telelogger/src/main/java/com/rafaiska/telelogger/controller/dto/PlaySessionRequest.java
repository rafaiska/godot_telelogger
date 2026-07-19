package com.rafaiska.telelogger.controller.dto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlaySessionRequest(
		@NotBlank @Size(max = 150) String randomSeed,
		Map<String, Object> preferences,
		Map<String, Object> gameState,
		List<@Valid EntityCommandRequest> commands) {

	public PlaySessionRequest {
		preferences = preferences == null ? new LinkedHashMap<>() : preferences;
		gameState = gameState == null ? new LinkedHashMap<>() : gameState;
		commands = commands == null ? List.of() : commands;
	}
}
