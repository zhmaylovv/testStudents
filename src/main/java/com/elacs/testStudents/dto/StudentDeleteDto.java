package com.elacs.testStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDeleteDto {
    private Long studentId;
    private Long groupId;
}
