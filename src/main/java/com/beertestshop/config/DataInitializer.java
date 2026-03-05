package com.beertestshop.config;

import com.beertestshop.service.FeedbackService;
import com.beertestshop.service.ProductService;
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

    private final ProductService productService;
    private final FeedbackService feedbackService;

    /**
     * CommandLineRunner для инициализации тестовых данных после запуска приложения.
     */
    @Bean
    CommandLineRunner initTestData() {
        return args -> {
            log.info("=== Starting test data initialization ===");

            productService.initTestData();
            feedbackService.initTestData();

            log.info("=== Test data initialization completed ===");
        };
    }
}
