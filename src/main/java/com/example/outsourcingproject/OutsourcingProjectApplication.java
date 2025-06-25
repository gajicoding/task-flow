package com.example.outsourcingproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class OutsourcingProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(OutsourcingProjectApplication.class, args);
    }

}
