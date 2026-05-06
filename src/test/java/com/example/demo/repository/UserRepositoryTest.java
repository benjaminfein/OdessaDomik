package com.example.demo.repository;

import com.example.demo.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User client;
    private User admin;
    private User unconfirmedOld;
    private User unconfirmedNew;

    @BeforeEach
    void setUp() {
        client = new User();
        client.setUsername("alice");
        client.setEmail("alice@test.com");
        client.setRole("client");
        client.setName("Alice");
        client.setPassword("pwd");
        client.setEmailConfirmed(true);
        client.setDateOfCreated(new Date());
        client = userRepository.save(client);

        admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@test.com");
        admin.setRole("admin");
        admin.setName("Admin");
        admin.setPassword("pwd");
        admin.setEmailConfirmed(true);
        admin.setDateOfCreated(new Date());
        admin = userRepository.save(admin);

        // Created 2 days ago, not confirmed
        unconfirmedOld = new User();
        unconfirmedOld.setUsername("old_unconfirmed");
        unconfirmedOld.setEmail("old@test.com");
        unconfirmedOld.setRole("client");
        unconfirmedOld.setName("Old");
        unconfirmedOld.setPassword("pwd");
        unconfirmedOld.setEmailConfirmed(false);
        unconfirmedOld.setDateOfCreated(Date.from(Instant.now().minusSeconds(2 * 24 * 60 * 60)));
        unconfirmedOld = userRepository.save(unconfirmedOld);

        // Created just now, not confirmed
        unconfirmedNew = new User();
        unconfirmedNew.setUsername("new_unconfirmed");
        unconfirmedNew.setEmail("new@test.com");
        unconfirmedNew.setRole("client");
        unconfirmedNew.setName("New");
        unconfirmedNew.setPassword("pwd");
        unconfirmedNew.setEmailConfirmed(false);
        unconfirmedNew.setDateOfCreated(new Date());
        unconfirmedNew = userRepository.save(unconfirmedNew);
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenFound() {
        Optional<User> result = userRepository.findByEmail("alice@test.com");

        assertTrue(result.isPresent());
        assertEquals("alice", result.get().getUsername());
    }

    @Test
    void findByEmail_ShouldReturnEmpty_WhenNotFound() {
        Optional<User> result = userRepository.findByEmail("nobody@test.com");

        assertFalse(result.isPresent());
    }

    @Test
    void findByRole_ShouldReturnUsersWithMatchingRole() {
        List<User> clients = userRepository.findByRole("client");

        assertTrue(clients.stream().anyMatch(u -> u.getEmail().equals("alice@test.com")));
        assertTrue(clients.stream().noneMatch(u -> u.getEmail().equals("admin@test.com")));
    }

    @Test
    void findByRole_ShouldReturnAdmins() {
        List<User> admins = userRepository.findByRole("admin");

        assertEquals(1, admins.size());
        assertEquals("admin@test.com", admins.get(0).getEmail());
    }

    @Test
    void findByEmailConfirmedFalseAndDateOfCreatedBefore_ShouldReturnOldUnconfirmed() {
        Date cutoff = Date.from(Instant.now().minusSeconds(24 * 60 * 60));

        List<User> result = userRepository.findByEmailConfirmedFalseAndDateOfCreatedBefore(cutoff);

        assertEquals(1, result.size());
        assertEquals("old@test.com", result.get(0).getEmail());
    }

    @Test
    void findByEmailConfirmedFalseAndDateOfCreatedBefore_ShouldReturnEmpty_WhenNoneOldEnough() {
        Date cutoff = Date.from(Instant.now().minusSeconds(3 * 24 * 60 * 60));

        List<User> result = userRepository.findByEmailConfirmedFalseAndDateOfCreatedBefore(cutoff);

        assertTrue(result.isEmpty());
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenExists() {
        assertTrue(userRepository.existsByEmail("alice@test.com"));
    }

    @Test
    void existsByEmail_ShouldReturnFalse_WhenNotExists() {
        assertFalse(userRepository.existsByEmail("nobody@test.com"));
    }

    @Test
    void existsByUsername_ShouldReturnTrue_WhenExists() {
        assertTrue(userRepository.existsByUsername("alice"));
    }

    @Test
    void existsByUsername_ShouldReturnFalse_WhenNotExists() {
        assertFalse(userRepository.existsByUsername("nobody"));
    }
}
