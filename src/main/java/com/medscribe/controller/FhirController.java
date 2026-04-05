package com.medscribe.controller;

import com.medscribe.model.SoapNote;
import com.medscribe.service.FhirBuilderService;
import com.medscribe.repository.SoapNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fhir")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class FhirController {

    private final FhirBuilderService fhirBuilderService;
    private final SoapNoteRepository soapNoteRepository;

    @GetMapping("/export/{sessionId}")
    public ResponseEntity<String> exportFhir(
            @PathVariable Long sessionId) {

        SoapNote soapNote = soapNoteRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("SOAP note not found"));

        String fhirBundle = fhirBuilderService.buildFhirBundle(soapNote);

        soapNote.setFhirBundle(fhirBundle);
        soapNoteRepository.save(soapNote);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fhirBundle);
    }

    @GetMapping("/download/{sessionId}")
    public ResponseEntity<byte[]> downloadFhir(
            @PathVariable Long sessionId) {

        SoapNote soapNote = soapNoteRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("SOAP note not found"));

        String fhirBundle = fhirBuilderService.buildFhirBundle(soapNote);
        byte[] bytes = fhirBundle.getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDispositionFormData("attachment",
                "fhir-bundle-session-" + sessionId + ".json");

        return ResponseEntity.ok()
                .headers(headers)
                .body(bytes);
    }
}