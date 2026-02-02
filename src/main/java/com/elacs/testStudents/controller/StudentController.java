package com.elacs.testStudents.controller;

import com.elacs.testStudents.dto.StudentAddDto;
import com.elacs.testStudents.dto.StudentDeleteDto;
import com.elacs.testStudents.model.Student;
import com.elacs.testStudents.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class StudentController {

    private final StudentService service;

    @GetMapping("/students")
    public String getAllStudentsByGroup(@RequestParam(name = "group_id") Long groupId,
                                        @RequestParam(name = "group_number") String groupNumber,
                                        Model model) {

        List<Student> allStudentsByGroupId = service.findAllStudentsByGroupId(groupId);
        model.addAttribute("group_id", groupId);
        model.addAttribute("group_number", groupNumber);
        model.addAttribute("students", allStudentsByGroupId);
        return "studentsGroupView";
    }

    @PostMapping("/students/add")
    public String addStudent(@RequestBody StudentAddDto request, Model model) {
        List<Student> students = service.saveStudentAndRefresh(request.getName(), request.getGroupId());
        model.addAttribute("students", students);
        return "studentsGroupView :: studentList";
    }

    @PostMapping("/students/delete")
    public String deleteStudent(@RequestBody StudentDeleteDto request, Model model) {
        List<Student> students = service.deleteStudentAndRefresh(request.getStudentId(), request.getGroupId());
        model.addAttribute("students", students);
        return "studentsGroupView :: studentList";
    }
}
