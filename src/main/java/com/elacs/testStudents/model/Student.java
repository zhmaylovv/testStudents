package com.elacs.testStudents.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name")
    private String fullName;

    @DateTimeFormat(pattern = "dd-MM-yyyy")
    @Column(name = "added_date")
    private LocalDate addedDate;

    @Column(name = "student_group_id")
    private Long studentGroupId;
}
