package com.medscribe.controller;

import com.medscribe.model.Session;
import com.medscribe.service.WhisperService;
import com.medscribe.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transcription")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class TranscriptionController {

    private final WhisperService whisperService;
    private final SessionRepository sessionRepository;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Medical Scribe API is running!");
    }

    @PostMapping("/session/start")
    public ResponseEntity<Session> startSession(
            @RequestParam String doctorName,
            @RequestParam String patientName,
            @RequestParam String patientAge) {

        Session session = new Session();
        session.setDoctorName(doctorName);
        session.setPatientName(patientName);
        session.setPatientAge(patientAge);
        session.setStartTime(LocalDateTime.now());
        session.setStatus(Session.SessionStatus.RECORDING);

        Session saved = sessionRepository.save(session);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/session/{sessionId}/upload-audio")
    public ResponseEntity<String> uploadAudio(
            @PathVariable Long sessionId,
            @RequestParam("audio") MultipartFile audioFile) {

        try {
            String transcript = whisperService.transcribe(audioFile);

            Session session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new RuntimeException("Session not found"));
            session.setRawTranscript(transcript);
            session.setStatus(Session.SessionStatus.PROCESSING);
            session.setEndTime(LocalDateTime.now());
            sessionRepository.save(session);

            return ResponseEntity.ok(transcript);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Transcription failed: " + e.getMessage());
        }
    }

    @PostMapping("/session/{sessionId}/set-transcript")
    public ResponseEntity<Session> setTranscript(
            @PathVariable Long sessionId,
            @RequestBody String transcript) {

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        session.setRawTranscript(transcript);
        session.setEndTime(LocalDateTime.now());
        session.setStatus(Session.SessionStatus.PROCESSING);
        sessionRepository.save(session);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<Session> getSession(@PathVariable Long sessionId) {
        return sessionRepository.findById(sessionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<Session>> getAllSessions() {
        return ResponseEntity.ok(sessionRepository.findAll());
    }

    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<String> deleteSession(@PathVariable Long sessionId) {
        sessionRepository.deleteById(sessionId);
        return ResponseEntity.ok("Session deleted");
    }
}