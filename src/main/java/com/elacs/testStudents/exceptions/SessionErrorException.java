package com.elacs.testStudents.exceptions;

public class SessionErrorException extends RuntimeException {
    public SessionErrorException(String message) {
        super(message);
    }
}
