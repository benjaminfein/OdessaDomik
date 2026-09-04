package com.example.demo.service;

import com.example.demo.exception.ReservationsLockedDownException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReservationRateLimiterTest {

    @Test
    void checkAndRecordAttempt_ShouldNotThrow_WhenUnderThresholds() {
        ReservationRateLimiter limiter = new ReservationRateLimiter();

        for (int i = 0; i < 10; i++) {
            assertDoesNotThrow(limiter::checkAndRecordAttempt);
        }
    }

    @Test
    void checkAndRecordAttempt_ShouldThrow_WhenExceedingPerMinuteThreshold() {
        ReservationRateLimiter limiter = new ReservationRateLimiter();

        assertThrows(ReservationsLockedDownException.class, () -> {
            for (int i = 0; i < 12; i++) {
                limiter.checkAndRecordAttempt();
            }
        });
    }

    @Test
    void checkAndRecordAttempt_ShouldKeepThrowing_WhileLockdownActive() {
        ReservationRateLimiter limiter = new ReservationRateLimiter();

        assertThrows(ReservationsLockedDownException.class, () -> {
            for (int i = 0; i < 12; i++) {
                limiter.checkAndRecordAttempt();
            }
        });

        assertThrows(ReservationsLockedDownException.class, limiter::checkAndRecordAttempt);
    }
}
