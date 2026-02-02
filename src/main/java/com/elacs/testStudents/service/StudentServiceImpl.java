package com.elacs.testStudents.service;

import com.elacs.testStudents.model.Student;
import com.elacs.testStudents.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository repository;

    @Override
    public List<Student> findAllStudentsByGroupId(Long id) {
        return repository.findByStudentGroupIdOrderByFullNameAsc(id);
    }

    @Override
    @Transactional
    public List<Student> saveStudentAndRefresh(String fullName, Long groupId) {
        Student student = Student.builder()
                .fullName(fullName)
                .studentGroupId(groupId)
                .addedDate(LocalDate.now())
                .build();
        repository.save(student);
        return repository.findByStudentGroupIdOrderByFullNameAsc(groupId);
    }

    @Override
    @Transactional
    public List<Student> deleteStudentAndRefresh(Long studentId, Long groupId) {
        repository.deleteById(studentId);
        return repository.findByStudentGroupIdOrderByFullNameAsc(groupId);
    }

}
