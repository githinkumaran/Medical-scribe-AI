package com.medscribe.repository;

import com.medscribe.model.SoapNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface SoapNoteRepository extends JpaRepository<SoapNote, Long> {

    @Query("SELECT s FROM SoapNote s WHERE s.session.id = :sessionId ORDER BY s.createdAt DESC LIMIT 1")
    Optional<SoapNote> findBySessionId(Long sessionId);
}