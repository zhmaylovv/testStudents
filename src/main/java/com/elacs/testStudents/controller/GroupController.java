package com.elacs.testStudents.controller;

import com.elacs.testStudents.dto.GroupViewDto;
import com.elacs.testStudents.model.Group;
import com.elacs.testStudents.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @GetMapping("/groups")
    public String getAllGroups(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               Model model) {
        Page<GroupViewDto> groupPage = groupService.getGroupViewDtoPage(page, size);
        model.addAttribute("groupPage", groupPage);
        model.addAttribute("groupViewDtoList", groupPage.getContent());
        return "groupsView";
    }

    @PostMapping("/groups")
    public String createGroup(String groupNumber) {
        Group group = groupService.saveStudentGroup(groupNumber);
        return "redirect:/students?group_id=" + group.getId() + "&group_number=" + groupNumber;
    }
}
