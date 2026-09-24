package com.rafaiska.telelogger.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rafaiska.telelogger.controller.dto.CreateEntityCommandRequest;
import com.rafaiska.telelogger.domain.EntityCommand;
import com.rafaiska.telelogger.domain.PlaySession;
import com.rafaiska.telelogger.repository.EntityCommandRepository;
import com.rafaiska.telelogger.repository.PlaySessionRepository;

@Service
public class EntityCommandService {

    private final EntityCommandRepository commands;
    private final PlaySessionRepository sessions;

    public EntityCommandService(EntityCommandRepository commands, PlaySessionRepository sessions) {
        this.commands = commands;
        this.sessions = sessions;
    }

    @Transactional
    public Long create(CreateEntityCommandRequest request) {
        PlaySession session = sessions.findById(request.session())
                .orElseThrow(() -> new PlaySessionNotFoundException(request.session()));
        EntityCommand command = new EntityCommand(session, request.entityId(),
                request.entityState(), request.commandType(), request.timestampMs());
        return commands.save(command).getId();
    }
}
