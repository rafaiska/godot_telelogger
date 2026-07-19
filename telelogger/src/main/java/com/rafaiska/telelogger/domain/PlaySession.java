package com.rafaiska.telelogger.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "play_sessions")
public class PlaySession {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "random_seed", nullable = false, length = 150)
	private String randomSeed;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false)
	private Map<String, Object> preferences = new LinkedHashMap<>();

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "game_state", nullable = false)
	private Map<String, Object> gameState = new LinkedHashMap<>();

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("timestampMs ASC")
	private List<EntityCommand> commands = new ArrayList<>();

	protected PlaySession() {
	}

	public PlaySession(String randomSeed, Map<String, Object> preferences, Map<String, Object> gameState) {
		this.randomSeed = randomSeed;
		this.preferences = new LinkedHashMap<>(preferences);
		this.gameState = new LinkedHashMap<>(gameState);
	}

	public void addCommand(EntityCommand command) {
		commands.add(command);
		command.attachTo(this);
	}

	public Long getId() {
		return id;
	}

	public String getRandomSeed() {
		return randomSeed;
	}

	public Map<String, Object> getPreferences() {
		return preferences;
	}

	public Map<String, Object> getGameState() {
		return gameState;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
