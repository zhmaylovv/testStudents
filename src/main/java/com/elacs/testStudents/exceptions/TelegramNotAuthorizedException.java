package com.elacs.testStudents.exceptions;

public class TelegramNotAuthorizedException extends RuntimeException {
    public TelegramNotAuthorizedException(String message) {
        super(message);
    }
}
