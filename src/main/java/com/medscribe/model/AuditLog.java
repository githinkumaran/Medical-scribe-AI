package com.medscribe.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sessionId;
    private String action;      // STARTED, TRANSCRIBED, SOAP_GENERATED, APPROVED, EXPORTED
    private String performedBy;
    private LocalDateTime timestamp;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String details;
}