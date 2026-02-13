package com.elacs.testStudents.dto.fit;

import lombok.Builder;

@Builder
public record FitRefreshTokenRq(String token, String refresh) {
}
