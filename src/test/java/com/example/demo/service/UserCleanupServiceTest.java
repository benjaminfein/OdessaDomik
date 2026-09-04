package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCleanupServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks
    private UserCleanupService userCleanupService;

    @Test
    void deleteUnconfirmedUsers_ShouldDeleteUsersOlderThan24Hours() {
        User staleUser = new User(1L, "stale@test.com", null, "client", "Stale", "pwd");
        when(userRepository.findByEmailConfirmedFalseAndDateOfCreatedBefore(any(Date.class)))
                .thenReturn(List.of(staleUser));

        userCleanupService.deleteUnconfirmedUsers();

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(userRepository).deleteAll(captor.capture());
        assertEquals(1, captor.getValue().size());
    }

    @Test
    void deleteUnconfirmedUsers_ShouldDoNothing_WhenNoStaleUsers() {
        when(userRepository.findByEmailConfirmedFalseAndDateOfCreatedBefore(any(Date.class)))
                .thenReturn(List.of());

        userCleanupService.deleteUnconfirmedUsers();

        verify(userRepository).deleteAll(List.of());
    }

    @Test
    void deleteUnconfirmedUsers_ShouldQueryWithDateInThePast() {
        when(userRepository.findByEmailConfirmedFalseAndDateOfCreatedBefore(any(Date.class)))
                .thenReturn(List.of());

        long before = System.currentTimeMillis();
        userCleanupService.deleteUnconfirmedUsers();
        long after = System.currentTimeMillis();

        ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);
        verify(userRepository).findByEmailConfirmedFalseAndDateOfCreatedBefore(dateCaptor.capture());

        Date capturedDate = dateCaptor.getValue();
        assertTrue(capturedDate.getTime() < before);
        assertTrue(capturedDate.getTime() > before - 25 * 60 * 60 * 1000L);
    }
}
