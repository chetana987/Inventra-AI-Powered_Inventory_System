package com.inventra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InventraApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventraApplication.class, args);
    }
}
