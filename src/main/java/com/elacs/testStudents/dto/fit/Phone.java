package com.elacs.testStudents.dto.fit;

import lombok.Builder;

@Builder
public record Phone(String countryCode, String number) {
}
