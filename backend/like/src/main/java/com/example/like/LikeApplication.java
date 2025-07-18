package com.example.like;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan(basePackages = "com.example")
@EnableJpaRepositories(basePackages = "com.example")
@SpringBootApplication
public class LikeApplication {
    public static void main(String[] args) {
        SpringApplication.run(LikeApplication.class, args);
    }
}
