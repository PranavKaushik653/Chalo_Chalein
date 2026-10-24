package com.travel.ai_agent.repository;

import com.travel.ai_agent.model.Organizer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrganizerRepository extends JpaRepository<Organizer, UUID> {
}