package com.medverify.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medverify.entity.AuditLog;
import com.medverify.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour AuditLogService
 */
@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuditLogService auditLogService;

    @BeforeEach
    void setUp() {
        // Note: AuditLogService has its own ObjectMapper instance
        // We'll need to use ReflectionTestUtils or test without mocking ObjectMapper
    }

    @Test
    void log_ValidData_ShouldSaveAuditLog() throws Exception {
        // Given
        String username = "admin@example.com";
        String action = "UPDATE";
        String entityType = "User";
        UUID entityId = UUID.randomUUID();
        String oldValue = "Old data";
        String newValue = "New data";

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        auditLogService.log(username, action, entityType, entityId, oldValue, newValue);

        // Then
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(auditLogCaptor.capture());
        
        AuditLog savedLog = auditLogCaptor.getValue();
        assertThat(savedLog.getUsername()).isEqualTo(username);
        assertThat(savedLog.getAction()).isEqualTo(action);
        assertThat(savedLog.getEntityType()).isEqualTo(entityType);
        assertThat(savedLog.getEntityId()).isEqualTo(entityId);
        assertThat(savedLog.getTimestamp()).isNotNull();
    }

    @Test
    void log_NullValues_ShouldHandleGracefully() {
        // Given
        String username = "admin@example.com";
        String action = "DELETE";
        String entityType = "User";
        UUID entityId = UUID.randomUUID();

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        auditLogService.log(username, action, entityType, entityId, null, null);

        // Then
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());
        
        AuditLog savedLog = auditLogCaptor.getValue();
        assertThat(savedLog.getOldValue()).isNull();
        assertThat(savedLog.getNewValue()).isNull();
    }

    @Test
    void log_WithComplexObject_ShouldSerialize() {
        // Given
        String username = "admin@example.com";
        String action = "CREATE";
        String entityType = "Report";
        UUID entityId = UUID.randomUUID();
        
        Object oldValue = new TestData("old", 1);
        Object newValue = new TestData("new", 2);

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        auditLogService.log(username, action, entityType, entityId, oldValue, newValue);

        // Then
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());
        
        AuditLog savedLog = auditLogCaptor.getValue();
        assertThat(savedLog.getOldValue()).isNotNull();
        assertThat(savedLog.getNewValue()).isNotNull();
    }

    @Test
    void log_RepositoryException_ShouldNotPropagate() {
        // Given
        String username = "admin@example.com";
        String action = "UPDATE";
        String entityType = "User";
        UUID entityId = UUID.randomUUID();

        doThrow(new RuntimeException("DB error")).when(auditLogRepository).save(any(AuditLog.class));

        // When - Should not throw exception
        auditLogService.log(username, action, entityType, entityId, null, null);

        // Then - Verify the save was attempted
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    // Test class for complex object serialization
    static class TestData {
        private String name;
        private int value;

        TestData(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "TestData{name='" + name + "', value=" + value + "}";
        }
    }
}

