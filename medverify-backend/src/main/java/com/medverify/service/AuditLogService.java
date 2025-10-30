package com.medverify.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medverify.entity.AuditLog;
import com.medverify.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service pour journaliser les actions d'administration
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Enregistrer action admin dans audit log
     */
    @Async
    public void log(String username, String action, String entityType,
            UUID entityId, Object oldValue, Object newValue) {

        try {
            AuditLog auditLog = AuditLog.builder()
                    .username(username)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .oldValue(oldValue != null ? serializeToJson(oldValue) : null)
                    .newValue(newValue != null ? serializeToJson(newValue) : null)
                    .timestamp(java.time.Instant.now())
                    .build();

            auditLogRepository.save(auditLog);

        } catch (Exception e) {
            log.error("Error saving audit log: {}", e.getMessage());
        }
    }

    private String serializeToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            return object.toString();
        }
    }
}


