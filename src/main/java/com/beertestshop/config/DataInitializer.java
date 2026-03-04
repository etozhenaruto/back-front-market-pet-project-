package com.beertestshop.config;

import com.beertestshop.service.InMemoryFeedbackService;
import com.beertestshop.service.InMemoryProductService;
import com.beertestshop.service.InMemoryUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация для инициализации тестовых данных при старте приложения.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final InMemoryUserService userService;
    private final InMemoryProductService productService;
    private final InMemoryFeedbackService feedbackService;

    /**
     * CommandLineRunner для инициализации тестовых данных после запуска приложения.
     */
    @Bean
    CommandLineRunner initTestData() {
        return args -> {
            log.info("=== Starting test data initialization ===");

            userService.initTestData();
            productService.initTestData();
            feedbackService.initTestData();

            log.info("=== Test data initialization completed ===");
        };
    }
}
