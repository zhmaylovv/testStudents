package com.elacs.testStudents.repository;

import com.elacs.testStudents.model.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Page<Group> findAllByOrderByAddedDateDesc(Pageable pageable);
}
