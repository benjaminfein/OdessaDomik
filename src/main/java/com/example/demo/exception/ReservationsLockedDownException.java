package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class ReservationsLockedDownException extends RuntimeException {
    public ReservationsLockedDownException(String message) {
        super(message);
    }
}
