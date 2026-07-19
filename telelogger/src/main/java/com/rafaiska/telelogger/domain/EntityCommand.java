package com.rafaiska.telelogger.domain;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "entity_commands")
public class EntityCommand {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	private PlaySession session;

	@Column(name = "entity_id", nullable = false, length = 150)
	private String entityId;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "entity_state", nullable = false)
	private Map<String, Object> entityState = new LinkedHashMap<>();

	@Column(name = "command_type", nullable = false, length = 100)
	private String commandType;

	@Column(name = "timestamp_ms", nullable = false)
	private Long timestampMs;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected EntityCommand() {
	}

	public EntityCommand(String entityId, Map<String, Object> entityState, String commandType, Long timestampMs) {
		this.entityId = entityId;
		this.entityState = new LinkedHashMap<>(entityState);
		this.commandType = commandType;
		this.timestampMs = timestampMs;
	}

	void attachTo(PlaySession session) {
		this.session = session;
	}
}
