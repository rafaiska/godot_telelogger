package com.rafaiska.telelogger.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.rafaiska.telelogger.domain.EntityCommand;

public interface EntityCommandRepository extends JpaRepository<EntityCommand, Long> {
}
