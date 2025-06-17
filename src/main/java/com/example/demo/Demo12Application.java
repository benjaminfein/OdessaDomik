package com.example.demo;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.demo.repository")
@EntityScan(basePackages = "com.example.demo.model")
@EnableScheduling
public class Demo12Application {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure()
                .directory("/root/OdessaDomik/")
                .filename(".env")
                .ignoreIfMissing()
                .load();

        Map<String, Object> envProps = new HashMap<>();
        dotenv.entries().forEach(entry -> {
            envProps.put(entry.getKey(), entry.getValue());
            System.setProperty(entry.getKey(), entry.getValue()); // ← полезно для отладки
        });

        System.out.println("[DEBUG]EMAIL_PASSWORD = " + dotenv.get("EMAIL_PASSWORD"));
        dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
        System.out.println("frontend.url from System: " + System.getProperty("frontend.url"));
        SpringApplication app = new SpringApplication(Demo12Application.class);
        app.setDefaultProperties(envProps);
        app.run(args);
    }
}
