package com.example.demo.service;

import com.example.demo.exception.ReservationsLockedDownException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class ReservationRateLimiter {
    private static final int MAX_PER_MINUTE = 10;
    private static final int MAX_PER_FIVE_MINUTES = 30;
    private static final long LOCKDOWN_MINUTES = 10;

    private final Deque<Instant> attempts = new ConcurrentLinkedDeque<>();
    private volatile Instant lockdownUntil;

    public synchronized void checkAndRecordAttempt() {
        Instant now = Instant.now();

        if (lockdownUntil != null && now.isBefore(lockdownUntil)) {
            throw new ReservationsLockedDownException("Reservations are temporarily locked down due to suspicious activity.");
        }

        attempts.addLast(now);
        pruneOlderThan(now.minus(5, ChronoUnit.MINUTES));

        long lastMinuteCount = attempts.stream()
                .filter(t -> t.isAfter(now.minus(1, ChronoUnit.MINUTES)))
                .count();
        long lastFiveMinutesCount = attempts.size();

        if (lastMinuteCount > MAX_PER_MINUTE || lastFiveMinutesCount > MAX_PER_FIVE_MINUTES) {
            lockdownUntil = now.plus(LOCKDOWN_MINUTES, ChronoUnit.MINUTES);
            throw new ReservationsLockedDownException("Reservations are temporarily locked down due to suspicious activity.");
        }
    }

    private void pruneOlderThan(Instant cutoff) {
        while (!attempts.isEmpty() && attempts.peekFirst().isBefore(cutoff)) {
            attempts.pollFirst();
        }
    }
}
