package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCleanupService {
    private final UserRepository userRepository;

    // Runs every 6 hours; deletes accounts that were not email-confirmed within 24 hours
    @Scheduled(fixedRate = 6 * 60 * 60 * 1000)
    public void deleteUnconfirmedUsers() {
        Instant nowMinus24h = Instant.now().minusSeconds(24 * 60 * 60);

        List<User> unconfirmedUsers = userRepository.findByEmailConfirmedFalseAndDateOfCreatedBefore(Date.from(nowMinus24h));

        userRepository.deleteAll(unconfirmedUsers);

        log.info("[UserCleanupService] Deleted {} unconfirmed accounts", unconfirmedUsers.size());
    }
}
