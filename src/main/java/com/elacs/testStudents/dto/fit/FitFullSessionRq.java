package com.elacs.testStudents.dto.fit;

import lombok.Builder;

@Builder
public record FitFullSessionRq(String token, String verificationCode, String externalCompanySource, String type) {
}
