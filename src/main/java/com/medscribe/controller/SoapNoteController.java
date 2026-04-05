package com.medscribe.controller;

import com.medscribe.model.Session;
import com.medscribe.model.SoapNote;
import com.medscribe.service.LLMService;
import com.medscribe.service.SoapParserService;
import com.medscribe.repository.SessionRepository;
import com.medscribe.repository.SoapNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/soap")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class SoapNoteController {

    private final LLMService llmService;
    private final SoapParserService soapParserService;
    private final SessionRepository sessionRepository;
    private final SoapNoteRepository soapNoteRepository;

    @PostMapping("/generate/{sessionId}")
    public ResponseEntity<SoapNote> generateSoapNote(
            @PathVariable Long sessionId) {

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        String transcript = session.getRawTranscript();
        if (transcript == null || transcript.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String rawSoap = llmService.generateSoapNote(transcript);
        SoapNote soapNote = soapParserService.parse(rawSoap, session);
        soapNote.setCreatedAt(LocalDateTime.now());

        SoapNote saved = soapNoteRepository.save(soapNote);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<SoapNote> getSoapNote(
            @PathVariable Long sessionId) {
        return soapNoteRepository.findBySessionId(sessionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{soapNoteId}/approve")
    public ResponseEntity<SoapNote> approveSoapNote(
            @PathVariable Long soapNoteId) {
        SoapNote soapNote = soapNoteRepository.findById(soapNoteId)
                .orElseThrow(() -> new RuntimeException("SOAP note not found"));
        soapNote.setDoctorApproved(true);
        return ResponseEntity.ok(soapNoteRepository.save(soapNote));
    }

    @PutMapping("/{soapNoteId}/edit")
    public ResponseEntity<SoapNote> editSoapNote(
            @PathVariable Long soapNoteId,
            @RequestBody SoapNote updatedNote) {
        SoapNote existing = soapNoteRepository.findById(soapNoteId)
                .orElseThrow(() -> new RuntimeException("SOAP note not found"));
        existing.setSubjective(updatedNote.getSubjective());
        existing.setObjective(updatedNote.getObjective());
        existing.setAssessment(updatedNote.getAssessment());
        existing.setPlan(updatedNote.getPlan());
        return ResponseEntity.ok(soapNoteRepository.save(existing));
    }
}