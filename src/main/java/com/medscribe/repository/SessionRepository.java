package com.medscribe.repository;

import com.medscribe.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByDoctorName(String doctorName);
}