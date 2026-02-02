package com.elacs.testStudents.repository;

import com.elacs.testStudents.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByStudentGroupIdOrderByFullNameAsc(Long group);
}
