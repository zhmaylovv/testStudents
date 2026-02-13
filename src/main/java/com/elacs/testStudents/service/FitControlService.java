package com.elacs.testStudents.service;

import com.elacs.testStudents.dto.fit.pojotranings.GroupTrainingSchedule;

import java.time.LocalDateTime;
import java.util.List;

public interface FitControlService {
    String getTempToken();

    void getFullSession(String tempToken, String code);

    void refreshToken();

    boolean isSessionAlive();

    void subscribe(String id, LocalDateTime date);

    List<GroupTrainingSchedule> getTrainingList();

    void setSchedulerTask(String id, LocalDateTime dateTime);
}
