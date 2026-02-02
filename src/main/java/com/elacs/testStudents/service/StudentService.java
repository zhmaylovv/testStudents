package com.elacs.testStudents.service;

import com.elacs.testStudents.model.Student;

import java.util.List;

public interface StudentService {
    List<Student> findAllStudentsByGroupId(Long id);

    List<Student> saveStudentAndRefresh(String fullName, Long groupId);

    List<Student> deleteStudentAndRefresh(Long studentId, Long groupId);
}
