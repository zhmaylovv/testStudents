package com.elacs.testStudents.controller;

import com.elacs.testStudents.exceptions.RefreshTokenErrorException;
import com.elacs.testStudents.exceptions.SessionErrorException;
import com.elacs.testStudents.exceptions.VerificationAlreadySentException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(VerificationAlreadySentException.class)
    public String handleVerificationError(VerificationAlreadySentException ex, Model model) {
        model.addAttribute("errorMessage", "Вроде отправили уже код, если придет введи, если нет, повтори через пару минут");
        return "fitView";
    }

    @ExceptionHandler(RefreshTokenErrorException.class)
    public String handleRefreshTokenError(RefreshTokenErrorException ex, Model model) {
        return "redirect:/fit/code";
    }

    @ExceptionHandler(SessionErrorException.class)
    public String handleSessionError(SessionErrorException ex, Model model) {
        return "redirect:/fit/code";
    }
}
