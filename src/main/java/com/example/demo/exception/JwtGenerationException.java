package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class JwtGenerationException extends RuntimeException {
    public JwtGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
