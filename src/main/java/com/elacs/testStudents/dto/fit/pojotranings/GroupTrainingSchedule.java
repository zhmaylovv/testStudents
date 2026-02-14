package com.elacs.testStudents.dto.fit.pojotranings;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record GroupTrainingSchedule(String guid,
                                    String title,
                                    int availablePlaces,
                                    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
                                    LocalDateTime date) {
}
