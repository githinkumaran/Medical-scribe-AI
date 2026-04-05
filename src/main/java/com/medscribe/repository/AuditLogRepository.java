package com.medscribe.repository;

import com.medscribe.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findBySessionIdOrderByTimestampAsc(Long sessionId);
}