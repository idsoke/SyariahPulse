package com.syariahpulse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SyariahPulseApplication {
    public static void main(String[] args) {
        SpringApplication.run(SyariahPulseApplication.class, args);
    }
}
