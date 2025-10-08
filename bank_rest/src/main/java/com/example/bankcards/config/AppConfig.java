package com.example.bankcards.config;

import com.example.bankcards.util.DataMasker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Random;

@Configuration
public class AppConfig implements WebMvcConfigurer {

    /**
     * Бин для работы с HTTP запросами к внешним API
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Бин для генерации случайных значений номеров карт, CVV кодов и т.д.
     */
    @Bean
    public Random random() {
        return new Random();
    }
}