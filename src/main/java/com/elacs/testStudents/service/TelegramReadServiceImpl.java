package com.elacs.testStudents.service;

import com.elacs.testStudents.model.TelegramTopic;
import com.elacs.testStudents.repository.TelegramTopicRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramReadServiceImpl implements TelegramReadService {

    private final TelegramTopicRepository topicRepository;
    private final TelegramClientService telegramClientService;
    private final TaskScheduler taskScheduler;

    @Value("${telegram.read.interval.minutes:5}")
    private int readIntervalMinutes;

    @PostConstruct
    public void schedulePeriodicRead() {
        if (telegramClientService.getAuthState() == TelegramClientService.AuthState.DISABLED) {
            return;
        }
        // First run after 2 minutes (give TDLib time to connect), then every N minutes
        taskScheduler.scheduleAtFixedRate(
                this::markAllEnabled,
                Instant.now().plus(Duration.ofMinutes(2)),
                Duration.ofMinutes(readIntervalMinutes));
        log.info("Telegram auto-read scheduled every {} minutes", readIntervalMinutes);
    }

    @Override
    public void markAllEnabled() {
        if (telegramClientService.getAuthState() != TelegramClientService.AuthState.AUTHORIZED) {
            log.debug("Skipping auto-read: not authorized (state={})",
                    telegramClientService.getAuthState());
            return;
        }

        List<TelegramTopic> topics = topicRepository.findAllByEnabledTrue();
        if (topics.isEmpty()) {
            log.debug("No enabled topics to mark as read");
            return;
        }

        log.info("Marking {} topic(s) as read", topics.size());
        for (TelegramTopic topic : topics) {
            try {
                telegramClientService.markTopicAsRead(topic.getChatId(), topic.getTopicId());
            } catch (Exception e) {
                log.error("Failed to mark topic id={} (chat={}, topic={}) as read",
                        topic.getId(), topic.getChatId(), topic.getTopicId(), e);
            }
        }
    }
}
