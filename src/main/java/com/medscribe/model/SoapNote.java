package com.medscribe.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "soap_notes")
@Data
public class SoapNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "session_id")
    private Session session;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String subjective;    // S — what patient says

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String objective;     // O — vitals, exam findings

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String assessment;    // A — diagnosis

    @Column(name = "[plan]", columnDefinition = "NVARCHAR(MAX)")
    private String plan;         // P — medicines, follow-up

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String fhirBundle;    // final FHIR JSON export

    private LocalDateTime createdAt;
    private Boolean doctorApproved = false;
}