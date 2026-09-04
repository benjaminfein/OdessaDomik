package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"dev", "test"})
class Demo12ApplicationTests {

    @MockBean
    private JavaMailSenderImpl javaMailSender;

    @Test
    void contextLoads() {
    }
}
