package com.elacs.testStudents.dto;

import com.elacs.testStudents.model.Group;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GroupViewDto {
    private Group group;
    private long studentCount;
}
