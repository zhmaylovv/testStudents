package com.elacs.testStudents.controller;

import com.elacs.testStudents.model.TelegramTopic;
import com.elacs.testStudents.repository.TelegramTopicRepository;
import com.elacs.testStudents.service.TelegramClientService;
import com.elacs.testStudents.service.TelegramReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/telegram")
@RequiredArgsConstructor
public class TelegramController {

    private final TelegramClientService telegramClientService;
    private final TelegramReadService telegramReadService;
    private final TelegramTopicRepository topicRepository;

    // -------------------------------------------------------------------------
    // Main page
    // -------------------------------------------------------------------------

    @GetMapping
    public String mainPage(Model model) {
        model.addAttribute("authState", telegramClientService.getAuthState());
        model.addAttribute("topics", topicRepository.findAll());
        return "telegramTopicsView";
    }

    // -------------------------------------------------------------------------
    // Auth
    // -------------------------------------------------------------------------

    @GetMapping("/auth")
    public String authPage(Model model) {
        model.addAttribute("authState", telegramClientService.getAuthState());
        return "telegramAuthView";
    }

    @PostMapping("/auth")
    public String submitCode(@RequestParam("code") String code) {
        telegramClientService.submitAuthCode(code.trim());
        return "redirect:/telegram";
    }

    @PostMapping("/auth/2fa")
    public String submit2fa(@RequestParam("password") String password) {
        telegramClientService.submit2faPassword(password);
        return "redirect:/telegram";
    }

    // -------------------------------------------------------------------------
    // Topics CRUD
    // -------------------------------------------------------------------------

    @PostMapping("/topics/add")
    public String addTopic(@RequestParam("chatId")     long   chatId,
                           @RequestParam("topicId")    long   topicId,
                           @RequestParam("chatTitle")  String chatTitle,
                           @RequestParam(value = "topicTitle", defaultValue = "") String topicTitle) {
        topicRepository.save(TelegramTopic.builder()
                .chatId(chatId)
                .topicId(topicId)
                .chatTitle(chatTitle.trim())
                .topicTitle(topicTitle.trim())
                .enabled(true)
                .build());
        return "redirect:/telegram";
    }

    @PostMapping("/topics/{id}/delete")
    public String deleteTopic(@PathVariable Long id) {
        topicRepository.deleteById(id);
        return "redirect:/telegram";
    }

    @PostMapping("/topics/{id}/toggle")
    public String toggleTopic(@PathVariable Long id) {
        topicRepository.findById(id).ifPresent(topic -> {
            topic.setEnabled(!topic.isEnabled());
            topicRepository.save(topic);
        });
        return "redirect:/telegram";
    }

    // -------------------------------------------------------------------------
    // Manual trigger
    // -------------------------------------------------------------------------

    @PostMapping("/read")
    public String readNow() {
        telegramReadService.markAllEnabled();
        return "redirect:/telegram";
    }

    // -------------------------------------------------------------------------
    // Optional: JSON list of chats (used by the add-topic helper dropdown)
    // -------------------------------------------------------------------------

    @GetMapping("/chats")
    @ResponseBody
    public List<TelegramClientService.ChatInfo> getChats() {
        return telegramClientService.getChats();
    }
}
