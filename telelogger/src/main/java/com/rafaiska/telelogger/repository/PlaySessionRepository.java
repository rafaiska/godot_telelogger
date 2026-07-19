package com.rafaiska.telelogger.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rafaiska.telelogger.domain.PlaySession;

public interface PlaySessionRepository extends JpaRepository<PlaySession, Long> {

	List<PlaySession> findAllByOrderByCreatedAtDesc();

	List<PlaySession> findByCreatedAtBetweenOrderByCreatedAtDesc(Instant start, Instant end);
}
