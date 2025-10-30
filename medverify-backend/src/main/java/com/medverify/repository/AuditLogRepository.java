package com.medverify.repository;

import com.medverify.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository pour les logs d'audit
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
}
