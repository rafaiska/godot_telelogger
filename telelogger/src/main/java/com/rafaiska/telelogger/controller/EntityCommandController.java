package com.rafaiska.telelogger.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rafaiska.telelogger.controller.dto.CreateEntityCommandRequest;
import com.rafaiska.telelogger.service.EntityCommandService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/commands")
public class EntityCommandController {

    private final EntityCommandService service;

    public EntityCommandController(EntityCommandService service) {
        this.service = service;
    }

    @PostMapping({"", "/"})
    public ResponseEntity<CreatedCommand> create(@Valid @RequestBody CreateEntityCommandRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new CreatedCommand(service.create(request)));
    }

    public record CreatedCommand(Long id) {
    }
}
