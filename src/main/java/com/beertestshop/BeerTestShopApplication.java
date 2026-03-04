package com.beertestshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Главное приложение BeerTestShop.
 * Pet-проект для практики автотестов.
 */
@SpringBootApplication
@EnableTransactionManagement
public class
BeerTestShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeerTestShopApplication.class, args);
    }
}
