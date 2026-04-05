package com.medscribe.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medscribe.model.SoapNote;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class FhirBuilderService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String buildFhirBundle(SoapNote soapNote) {
        try {
            Map<String, Object> bundle = new LinkedHashMap<>();
            bundle.put("resourceType", "Bundle");
            bundle.put("type", "document");
            bundle.put("timestamp", LocalDateTime.now()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            List<Map<String, Object>> entries = new ArrayList<>();

            // Encounter resource - the visit itself
            entries.add(buildEntry(buildEncounter(soapNote)));

            // Condition resource - the diagnosis (from Assessment)
            if (soapNote.getAssessment() != null
                    && !soapNote.getAssessment().isEmpty()) {
                entries.add(buildEntry(buildCondition(soapNote)));
            }

            // Observation resource - vitals (from Objective)
            if (soapNote.getObjective() != null
                    && !soapNote.getObjective().isEmpty()) {
                entries.add(buildEntry(buildObservation(soapNote)));
            }

            // MedicationRequest - prescriptions (from Plan)
            if (soapNote.getPlan() != null
                    && !soapNote.getPlan().isEmpty()) {
                entries.add(buildEntry(buildMedicationRequest(soapNote)));
            }

            // DocumentReference - the full SOAP note text
            entries.add(buildEntry(buildDocumentReference(soapNote)));

            bundle.put("entry", entries);

            return objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(bundle);

        } catch (Exception e) {
            throw new RuntimeException("Failed to build FHIR bundle: "
                    + e.getMessage());
        }
    }

    private Map<String, Object> buildEncounter(SoapNote soapNote) {
        Map<String, Object> encounter = new LinkedHashMap<>();
        encounter.put("resourceType", "Encounter");
        encounter.put("status", "finished");
        encounter.put("subject", Map.of(
                "display", soapNote.getSession().getPatientName()
        ));
        encounter.put("participant", List.of(Map.of(
                "individual", Map.of(
                        "display", soapNote.getSession().getDoctorName()
                )
        )));
        encounter.put("period", Map.of(
                "start", soapNote.getSession().getStartTime().toString(),
                "end", soapNote.getSession().getEndTime() != null
                        ? soapNote.getSession().getEndTime().toString()
                        : LocalDateTime.now().toString()
        ));
        return encounter;
    }

    private Map<String, Object> buildCondition(SoapNote soapNote) {
        Map<String, Object> condition = new LinkedHashMap<>();
        condition.put("resourceType", "Condition");
        condition.put("clinicalStatus", Map.of(
                "coding", List.of(Map.of(
                        "code", "active",
                        "display", "Active"
                ))
        ));
        condition.put("code", Map.of(
                "text", soapNote.getAssessment()
        ));
        condition.put("subject", Map.of(
                "display", soapNote.getSession().getPatientName()
        ));
        return condition;
    }

    private Map<String, Object> buildObservation(SoapNote soapNote) {
        Map<String, Object> observation = new LinkedHashMap<>();
        observation.put("resourceType", "Observation");
        observation.put("status", "final");
        observation.put("code", Map.of(
                "text", "Clinical Observations"
        ));
        observation.put("subject", Map.of(
                "display", soapNote.getSession().getPatientName()
        ));
        observation.put("valueString", soapNote.getObjective());
        return observation;
    }

    private Map<String, Object> buildMedicationRequest(SoapNote soapNote) {
        Map<String, Object> medication = new LinkedHashMap<>();
        medication.put("resourceType", "MedicationRequest");
        medication.put("status", "active");
        medication.put("intent", "order");
        medication.put("medicationCodeableConcept", Map.of(
                "text", soapNote.getPlan()
        ));
        medication.put("subject", Map.of(
                "display", soapNote.getSession().getPatientName()
        ));
        return medication;
    }

    private Map<String, Object> buildDocumentReference(SoapNote soapNote) {
        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("resourceType", "DocumentReference");
        doc.put("status", "current");
        doc.put("type", Map.of(
                "text", "SOAP Note"
        ));
        doc.put("subject", Map.of(
                "display", soapNote.getSession().getPatientName()
        ));

        String fullNote = "SUBJECTIVE:\n" + soapNote.getSubjective()
                + "\n\nOBJECTIVE:\n" + soapNote.getObjective()
                + "\n\nASSESSMENT:\n" + soapNote.getAssessment()
                + "\n\nPLAN:\n" + soapNote.getPlan();

        String encoded = Base64.getEncoder()
                .encodeToString(fullNote.getBytes());

        doc.put("content", List.of(Map.of(
                "attachment", Map.of(
                        "contentType", "text/plain",
                        "data", encoded
                )
        )));
        return doc;
    }

    private Map<String, Object> buildEntry(Map<String, Object> resource) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("resource", resource);
        return entry;
    }
}