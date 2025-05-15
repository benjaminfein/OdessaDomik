package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserCleanupService {
    private final UserRepository userRepository;

    // Запуск каждые 6 часов
    @Scheduled(fixedRate = 6 * 60 * 60 * 1000)
    public void deleteUnconfirmedUsers() {
        Instant nowMinus24h = Instant.now().minusSeconds(24 * 60 * 60);

        List<User> unconfirmedUsers = userRepository.findByEmailConfirmedFalseAndDateOfCreatedBefore(Date.from(nowMinus24h));

        userRepository.deleteAll(unconfirmedUsers);

        System.out.println("🧹 Удалено " + unconfirmedUsers.size() + " неподтвердженных акаунтов");
    }
}
