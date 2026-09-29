package com.rafaiska.telelogger.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:telelogger_cors;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "TELELOGGER_CORS_ALLOWED_ORIGINS=https://html-classic.itch.zone"
})
@AutoConfigureMockMvc
class CorsIntegrationTests {
    private static final String ORIGIN = "https://html-classic.itch.zone";

    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;

    @ParameterizedTest
    @ValueSource(strings = {"/api/sessions", "/api/sessions/", "/api/commands", "/api/commands/"})
    void allowsJsonPostPreflight(String endpoint) throws Exception {
        mvc.perform(options(endpoint)
                .header("Origin", ORIGIN)
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGIN))
                .andExpect(header().string("Access-Control-Allow-Methods", "POST"))
                .andExpect(header().string("Access-Control-Allow-Headers", "content-type"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Credentials"));
    }

    @Test
    void createsSessionAndCommandWithCorsResponseHeaders() throws Exception {
        mvc.perform(post("/api/sessions/").header("Origin", ORIGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"random_seed\":\"cors-test\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGIN));
        Long sessionId = jdbc.queryForObject("SELECT MAX(id) FROM play_sessions", Long.class);
        mvc.perform(post("/api/commands/").header("Origin", ORIGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"session": %d, "entity_id": "player", "command_type": "move", "timestamp_ms": 0}
                        """.formatted(sessionId)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGIN));
    }

    @Test
    void includesCorsHeadersOnValidationErrors() throws Exception {
        mvc.perform(post("/api/sessions/").header("Origin", ORIGIN)
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGIN));
    }

    @Test
    void rejectsUnconfiguredOrigin() throws Exception {
        mvc.perform(options("/api/sessions/").header("Origin", "https://untrusted.example")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void doesNotAuthorizeCrossOriginReads() throws Exception {
        mvc.perform(options("/api/sessions/").header("Origin", ORIGIN)
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }
}
