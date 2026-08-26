package com.example.demo.exception;

// Carries which field caused the conflict (email/username) so the frontend
// can highlight the specific form field instead of showing a generic error.
public class DuplicateUserException extends RuntimeException {
    private final String field;

    public DuplicateUserException(String message, String field) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
