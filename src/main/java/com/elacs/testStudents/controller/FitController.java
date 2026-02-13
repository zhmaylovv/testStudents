package com.elacs.testStudents.controller;

import com.elacs.testStudents.dto.fit.pojotranings.GroupTrainingSchedule;
import com.elacs.testStudents.service.FitControlService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class FitController {
    private final FitControlService controlService;

    @GetMapping("/fit")
    public String getFit(HttpSession session, Model model) {
        if (controlService.isSessionAlive()) {
            return "redirect:/fit/trainings";
        }
        controlService.refreshToken();
        if (controlService.isSessionAlive()) {
            return "redirect:/fit/trainings";
        }
        return "redirect:/fit/code";
    }

    @GetMapping("/fit/code")
    public String getFitMainPage(HttpSession session, Model model) {
        String tempToken = controlService.getTempToken();
        session.setAttribute("tempToken", tempToken);
        return "fitView";
    }

    @GetMapping("/fit/trainings")
    public String getTrainingsPage(HttpSession session, Model model) {
        List<GroupTrainingSchedule> trainingList = controlService.getTrainingList();
        model.addAttribute("trainingList", trainingList);
        return "fitButtonView";
    }

    @PostMapping("/fit/code")
    public String getCode(HttpSession session, String code, Model model) {
        String temptoken = (String) session.getAttribute("tempToken");
        controlService.getFullSession(temptoken, code);
        return "redirect:/fit/trainings";
    }

    @PostMapping("/fit/subscribe")
    public String subscribe(@RequestParam("guid") String guid,
                            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        controlService.setSchedulerTask(guid, date);
        return "fitSuccess";
    }
}
