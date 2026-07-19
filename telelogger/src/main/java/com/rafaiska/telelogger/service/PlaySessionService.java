package com.rafaiska.telelogger.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rafaiska.telelogger.controller.dto.EntityCommandRequest;
import com.rafaiska.telelogger.controller.dto.PlaySessionRequest;
import com.rafaiska.telelogger.domain.EntityCommand;
import com.rafaiska.telelogger.domain.PlaySession;
import com.rafaiska.telelogger.repository.PlaySessionRepository;

@Service
public class PlaySessionService {

	private final PlaySessionRepository repository;

	public PlaySessionService(PlaySessionRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public PlaySession create(PlaySessionRequest request) {
		PlaySession session = new PlaySession(request.randomSeed(), request.preferences(), request.gameState());
		request.commands().stream()
				.map(this::toEntity)
				.forEach(session::addCommand);
		return repository.save(session);
	}

	@Transactional(readOnly = true)
	public List<PlaySession> findAll() {
		return repository.findAllByOrderByCreatedAtDesc();
	}

	@Transactional(readOnly = true)
	public PlaySession findById(Long id) {
		return repository.findById(id)
				.orElseThrow(() -> new PlaySessionNotFoundException(id));
	}

	@Transactional(readOnly = true)
	public List<PlaySession> findBetween(Instant start, Instant end) {
		return repository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
	}

	private EntityCommand toEntity(EntityCommandRequest request) {
		return new EntityCommand(
				request.entityId(),
				request.entityState(),
				request.commandType(),
				request.timestampMs());
	}
}
