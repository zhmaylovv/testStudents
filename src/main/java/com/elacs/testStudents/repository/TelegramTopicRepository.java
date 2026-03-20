package com.elacs.testStudents.repository;

import com.elacs.testStudents.model.TelegramTopic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TelegramTopicRepository extends JpaRepository<TelegramTopic, Long> {

    List<TelegramTopic> findAllByEnabledTrue();
}
