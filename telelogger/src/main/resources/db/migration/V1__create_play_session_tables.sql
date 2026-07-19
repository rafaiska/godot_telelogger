CREATE TABLE play_sessions (
	id BIGSERIAL PRIMARY KEY,
	random_seed VARCHAR(150) NOT NULL,
	preferences JSONB NOT NULL DEFAULT '{}'::jsonb,
	game_state JSONB NOT NULL DEFAULT '{}'::jsonb,
	created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_play_sessions_created_at ON play_sessions (created_at DESC);

CREATE TABLE entity_commands (
	id BIGSERIAL PRIMARY KEY,
	session_id BIGINT NOT NULL REFERENCES play_sessions(id) ON DELETE CASCADE,
	entity_id VARCHAR(150) NOT NULL,
	entity_state JSONB NOT NULL DEFAULT '{}'::jsonb,
	command_type VARCHAR(100) NOT NULL,
	timestamp_ms BIGINT NOT NULL CHECK (timestamp_ms >= 0),
	created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_entity_commands_session_timestamp
	ON entity_commands (session_id, timestamp_ms);
