package com.rafaiska.telelogger.controller;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rafaiska.telelogger.controller.dto.PlaySessionRequest;
import com.rafaiska.telelogger.controller.dto.PlaySessionResponse;
import com.rafaiska.telelogger.domain.PlaySession;
import com.rafaiska.telelogger.service.PlaySessionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sessions")
public class PlaySessionController {

	private final PlaySessionService service;

	public PlaySessionController(PlaySessionService service) {
		this.service = service;
	}

	@PostMapping({"", "/"})
	public ResponseEntity<PlaySessionResponse> create(@Valid @RequestBody PlaySessionRequest request) {
		PlaySession session = service.create(request);
		return ResponseEntity
				.created(URI.create("/api/sessions/" + session.getId() + "/"))
				.body(PlaySessionResponse.from(session));
	}

	@GetMapping({"", "/"})
	public List<PlaySessionResponse> findAll() {
		return service.findAll().stream().map(PlaySessionResponse::from).toList();
	}

	@GetMapping({"/{id}", "/{id}/"})
	public PlaySessionResponse findById(@PathVariable Long id) {
		return PlaySessionResponse.from(service.findById(id));
	}

	@GetMapping({"/between", "/between/"})
	public List<PlaySessionResponse> findBetween(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
		if (start.isAfter(end)) {
			throw new InvalidDateRangeException();
		}
		return service.findBetween(start, end).stream().map(PlaySessionResponse::from).toList();
	}
}
