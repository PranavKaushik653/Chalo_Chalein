package com.chalochalein.backend.common;


import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final JdbcClient jdbcClient;

    public void log(UUID actor, String action, String entityType, String entityId){
        jdbcClient.sql("INSERT INTO audit_log (actor_user_id, action, entity_type, entity_id) VALUES (:a, :ac, :et, :eid)")
                .param("a", actor).param("ac", action).param("et", entityType).param("eid", entityId).update();
    }
}
