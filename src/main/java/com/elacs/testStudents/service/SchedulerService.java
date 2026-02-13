package com.elacs.testStudents.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Novosibirsk");

    private final TaskScheduler taskScheduler;
    private final FitControlService service;

    public void scheduleTask(String id, LocalDateTime dateTime) {
        ZonedDateTime zonedDateTime = dateTime.atZone(ZONE_ID).plusDays(-1);
        if (zonedDateTime.isBefore(ZonedDateTime.now())) {
            service.subscribe(id, dateTime);
            return;
        }
        log.info("Задача запланирована на (местное время): " + zonedDateTime);
        scheduleSessionTask(zonedDateTime);
        taskScheduler.schedule(() -> {
            service.subscribe(id, dateTime);
        }, zonedDateTime.toInstant());
    }

    private void scheduleSessionTask(ZonedDateTime dateTime) {
        ZonedDateTime zonedDateTime = dateTime.plusSeconds(-20);
        log.info("Задача прогрева сесии запланирована на (местное время): " + zonedDateTime);
        taskScheduler.schedule(() -> {
            if (!service.isSessionAlive()) service.refreshToken();
        }, zonedDateTime.toInstant());
    }

}
