package com.medscribe.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "sessions")
@Data
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String doctorName;
    private String patientName;
    private String patientAge;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String rawTranscript;

    private String audioFilePath;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    public enum SessionStatus {
        RECORDING, PROCESSING, COMPLETED
    }
}
