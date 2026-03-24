package com.examportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ExamPortalApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExamPortalApplication.class, args);
    }
}
