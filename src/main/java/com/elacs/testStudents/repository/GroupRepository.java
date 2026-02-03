package com.elacs.testStudents.repository;

import com.elacs.testStudents.dto.GroupViewDto;
import com.elacs.testStudents.model.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GroupRepository extends JpaRepository<Group, Long> {
    @Query(value = "SELECT new com.elacs.testStudents.dto.GroupViewDto(g, count(s)) " +
                    "FROM Group g " +
                    "LEFT JOIN Student s ON s.studentGroupId = g.id " +
                    "GROUP BY g",
                    countQuery = "SELECT count(g) FROM Group g")
    Page<GroupViewDto> findAllByOrderByAddedDateDescWithCount(Pageable pageable);
}
