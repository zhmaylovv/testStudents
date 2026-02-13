package com.elacs.testStudents.dto.fit;

public record Token(String iat, String jti, String gid, String cid, String ecs, boolean isr, String exp) {
}
