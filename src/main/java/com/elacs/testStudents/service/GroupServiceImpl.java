package com.elacs.testStudents.service;

import com.elacs.testStudents.model.Group;
import com.elacs.testStudents.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final GroupRepository repository;

    @Override
    public Page<Group> findAllOrderByAddedDateDesc(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repository.findAllByOrderByAddedDateDesc(pageable);
    }

    @Override
    public Group saveStudentGroup(String groupNumber) {
        Group group = Group.builder()
                .groupNumber(groupNumber)
                .addedDate(LocalDate.now())
                .build();
        return repository.save(group);
    }
}
