package com.rafaiska.telelogger.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class EntityCommandControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    private Long createSession() throws Exception {
        mockMvc.perform(post("/api/sessions/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"random_seed\":\"commands-test\"}"))
                .andExpect(status().isCreated());
        return jdbc.queryForObject("SELECT MAX(id) FROM play_sessions", Long.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/commands", "/api/commands/"})
    void persistsLoggerPayloadWithoutReplacingEarlierCommands(String endpoint) throws Exception {
        Long session = createSession();
        for (int timestamp = 0; timestamp < 2; timestamp++) {
            mockMvc.perform(post(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"session": %d, "entity_id": "player-1", "command_type": "move",
                             "entity_state": {"hp": 100, "position": [0, 1]}, "timestamp_ms": %d}
                            """.formatted(session, timestamp)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber());
        }
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM entity_commands WHERE session_id = ?", Long.class, session))
                .isEqualTo(2L);
        var command = jdbc.queryForMap(
                "SELECT entity_id, command_type, timestamp_ms, entity_state FROM entity_commands "
                        + "WHERE session_id = ? AND timestamp_ms = 1", session);
        assertThat(command.get("entity_id")).isEqualTo("player-1");
        assertThat(command.get("command_type")).isEqualTo("move");
        assertThat(((Number) command.get("timestamp_ms")).longValue()).isEqualTo(1L);
        String state = jdbc.queryForObject(
                "SELECT CAST(entity_state AS VARCHAR) FROM entity_commands "
                        + "WHERE session_id = ? AND timestamp_ms = 1", String.class, session);
        assertThat(state).contains("\"hp\":100", "\"position\":[0,1]");
    }

    @Test
    void defaultsMissingStateToEmptyObject() throws Exception {
        Long session = createSession();
        mockMvc.perform(post("/api/commands/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"session": %d, "entity_id": "SYSTEM", "command_type": "ERROR", "timestamp_ms": 0}
                        """.formatted(session)))
                .andExpect(status().isCreated());
        assertThat(jdbc.queryForObject(
                "SELECT CAST(entity_state AS VARCHAR) FROM entity_commands WHERE session_id = ?",
                String.class, session)).isEqualTo("{}");
    }

    @Test
    void rejectsUnknownSessionWithoutWritingCommand() throws Exception {
        Long before = jdbc.queryForObject("SELECT COUNT(*) FROM entity_commands", Long.class);
        mockMvc.perform(post("/api/commands/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"session": 9223372036854775807, "entity_id": "player-1",
                         "command_type": "move", "timestamp_ms": 0}
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").exists());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM entity_commands", Long.class)).isEqualTo(before);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"session\":null,\"entity_id\":\"p\",\"command_type\":\"m\",\"timestamp_ms\":0}",
            "{\"session\":0,\"entity_id\":\"p\",\"command_type\":\"m\",\"timestamp_ms\":0}",
            "{\"session\":1,\"entity_id\":\" \",\"command_type\":\"m\",\"timestamp_ms\":0}",
            "{\"session\":1,\"entity_id\":\"p\",\"command_type\":\" \",\"timestamp_ms\":0}",
            "{\"session\":1,\"entity_id\":\"p\",\"command_type\":\"m\"}",
            "{\"session\":1,\"entity_id\":\"p\",\"command_type\":\"m\",\"timestamp_ms\":-1}"
    })
    void rejectsInvalidPayloadWithoutWritingCommand(String payload) throws Exception {
        Long before = jdbc.queryForObject("SELECT COUNT(*) FROM entity_commands", Long.class);
        mockMvc.perform(post("/api/commands/")
                .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM entity_commands", Long.class)).isEqualTo(before);
    }
}
