package com.elacs.testStudents.repository;

import com.elacs.testStudents.model.ScheduleTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface ScheduleTaskRepository extends JpaRepository<ScheduleTask, Long> {
    @Transactional
    void deleteByTrainingId(String trainingId);
}
