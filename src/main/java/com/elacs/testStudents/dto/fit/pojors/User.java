package com.elacs.testStudents.dto.fit.pojors;

import java.util.List;

public record User(String guid, String userGuid, String clientGuid, boolean isRegistered, Phone phone, String email, String name, String patronymic, String surname, String gender, String birthDate, Object photoUrl, boolean isVerified, List<Object> clubMemberships, Object hash) {}
