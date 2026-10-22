package com.dysight.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DysightBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DysightBackendApplication.class, args);
    }
}