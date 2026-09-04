package com.example.demo.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.Instant;

@Getter
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UserBannedException extends RuntimeException {
    private final Instant bannedUntil;

    public UserBannedException(String message, Instant bannedUntil) {
        super(message);
        this.bannedUntil = bannedUntil;
    }
}
