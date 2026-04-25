package com.example.handmademarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HandmadeMarketBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HandmadeMarketBackendApplication.class, args);
    }
}
