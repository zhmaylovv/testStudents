package com.elacs.testStudents.dto.fit;

import lombok.Builder;

@Builder
public record FitStartSessionRq(Phone phone, String signature) {
}
