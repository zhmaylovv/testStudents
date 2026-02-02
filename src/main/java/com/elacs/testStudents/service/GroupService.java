package com.elacs.testStudents.service;

import com.elacs.testStudents.model.Group;
import org.springframework.data.domain.Page;

public interface GroupService {
    Page<Group> findAllOrderByAddedDateDesc(int page, int size);

    Group saveStudentGroup(String groupName);


}
