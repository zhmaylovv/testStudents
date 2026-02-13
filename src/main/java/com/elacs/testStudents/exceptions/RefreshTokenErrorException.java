package com.elacs.testStudents.exceptions;

public class RefreshTokenErrorException extends RuntimeException {
    public RefreshTokenErrorException(String message) {
        super(message);
    }
}
