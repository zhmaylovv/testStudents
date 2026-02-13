package com.elacs.testStudents.exceptions;

public class VerificationAlreadySentException extends RuntimeException {
    public VerificationAlreadySentException(String message) {
        super(message);
    }
}
