package com.elacs.testStudents.repository;

import com.elacs.testStudents.model.FitData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FitDataRepository extends JpaRepository<FitData, Long> {
}
