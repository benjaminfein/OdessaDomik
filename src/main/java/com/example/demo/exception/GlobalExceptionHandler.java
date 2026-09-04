package com.example.demo.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

// Central place for turning exceptions into JSON error responses, so every endpoint
// returns a consistent { "message": "..." } body instead of relying on Spring Boot's
// default error body (whose "message" field is suppressed unless
// server.error.include-message is enabled).
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApartmentNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleApartmentNotFound(ApartmentNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleReservationNotFound(ReservationNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ReservationConflictException.class)
    public ResponseEntity<Map<String, Object>> handleReservationConflict(ReservationConflictException e) {
        return error(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(MinStayViolationException.class)
    public ResponseEntity<Map<String, Object>> handleMinStayViolation(MinStayViolationException e) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", e.getMessage());
        body.put("minStay", e.getRequiredMinStay());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(UserBannedException.class)
    public ResponseEntity<Map<String, Object>> handleUserBanned(UserBannedException e) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", e.getMessage());
        body.put("bannedUntil", e.getBannedUntil().toString());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(ReservationsLockedDownException.class)
    public ResponseEntity<Map<String, Object>> handleReservationsLockedDown(ReservationsLockedDownException e) {
        return error(HttpStatus.TOO_MANY_REQUESTS, e.getMessage());
    }

    @ExceptionHandler(InvalidOrExpiredTokenException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidOrExpiredToken(InvalidOrExpiredTokenException e) {
        return error(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateUser(DuplicateUserException e) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", e.getMessage());
        body.put("field", e.getField());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // Internal/infrastructure failures (missing email template, SMTP failure, JWT signing
    // failure) — never the user's fault, so the client only gets a generic message while the
    // real cause is logged server-side.
    @ExceptionHandler({EmailTemplateNotFoundException.class, EmailDeliveryException.class, JwtGenerationException.class})
    public ResponseEntity<Map<String, Object>> handleInternalFailure(RuntimeException e) {
        log.error("Unhandled internal failure", e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again later.");
    }

    // Catch-all fallback for everything not explicitly handled above. Without this, unhandled
    // exceptions would fall through to Spring's default error handling and the client would
    // see "No message available" instead of a usable message.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception e) {
        log.error("Unhandled exception", e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again later.");
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }
}
