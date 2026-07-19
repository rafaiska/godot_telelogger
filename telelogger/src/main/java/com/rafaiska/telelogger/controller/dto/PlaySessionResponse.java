package com.rafaiska.telelogger.controller.dto;

import java.time.Instant;
import java.util.Map;

import com.rafaiska.telelogger.domain.PlaySession;

public record PlaySessionResponse(
		Long id,
		String randomSeed,
		Map<String, Object> preferences,
		Map<String, Object> gameState,
		Instant createdAt) {

	public static PlaySessionResponse from(PlaySession session) {
		return new PlaySessionResponse(
				session.getId(),
				session.getRandomSeed(),
				session.getPreferences(),
				session.getGameState(),
				session.getCreatedAt());
	}
}
