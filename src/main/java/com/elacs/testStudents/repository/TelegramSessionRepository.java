package com.elacs.testStudents.repository;

import com.elacs.testStudents.model.TelegramSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelegramSessionRepository extends JpaRepository<TelegramSession, Long> {
}
