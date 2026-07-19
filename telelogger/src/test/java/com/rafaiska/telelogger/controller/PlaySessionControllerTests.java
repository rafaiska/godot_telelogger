package com.rafaiska.telelogger.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PlaySessionControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void createsAndRetrievesPlaySessionWithNestedCommands() throws Exception {
		String payload = """
				{
				  "random_seed": "seed-123",
				  "preferences": {"target": "stealth"},
				  "game_state": {"level": 3},
				  "commands": [{
				    "entity_id": "player-1",
				    "entity_state": {"hp": 100},
				    "command_type": "move_forward",
				    "timestamp_ms": 120
				  }]
				}
				""";

		mockMvc.perform(post("/api/sessions/")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/api/sessions/1/"))
				.andExpect(jsonPath("$.random_seed").value("seed-123"))
				.andExpect(jsonPath("$.commands").doesNotExist());

		Long commandCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM entity_commands", Long.class);
		org.assertj.core.api.Assertions.assertThat(commandCount).isEqualTo(1);

		mockMvc.perform(get("/api/sessions/1/"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.preferences.target").value("stealth"));

		mockMvc.perform(get("/api/sessions/"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)));

		mockMvc.perform(get("/api/sessions/between/")
				.param("start", "2020-01-01T00:00:00Z")
				.param("end", "2030-01-01T00:00:00Z"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)));
	}

	@Test
	void rejectsInvalidDateRange() throws Exception {
		mockMvc.perform(get("/api/sessions/between/")
				.param("start", "2025-03-03T00:00:00Z")
				.param("end", "2025-03-02T00:00:00Z"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("start must be the same as or earlier than end."));
	}

	@Test
	void requiresRandomSeed() throws Exception {
		mockMvc.perform(post("/api/sessions")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").exists());
	}
}
