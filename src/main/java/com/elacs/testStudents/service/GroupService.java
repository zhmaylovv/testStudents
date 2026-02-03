package com.elacs.testStudents.service;

import com.elacs.testStudents.dto.GroupViewDto;
import com.elacs.testStudents.model.Group;
import org.springframework.data.domain.Page;

public interface GroupService {
    Page<GroupViewDto> getGroupViewDtoPage(int page, int size);

    Group saveStudentGroup(String groupName);
}
