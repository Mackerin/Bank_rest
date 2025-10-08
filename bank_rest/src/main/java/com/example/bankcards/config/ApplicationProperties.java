package com.example.bankcards.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app")
@Validated
public class ApplicationProperties {

    @NotNull(message = "Конфигурация JWT не может быть null")
    private Jwt jwt = new Jwt();

    @NotNull(message = "Конфигурация CORS не может быть null")
    private Cors cors = new Cors();

    @NotNull(message = "Конфигурация банка не может быть null")
    private Bank bank = new Bank();

    @NotNull(message = "Конфигурация безопасности не может быть null")
    private Security security = new Security();

    @NotNull(message = "Конфигурация API не может быть null")
    private Api api = new Api();

    @NotNull(message = "Конфигурация логирования не может быть null")
    private Logging logging = new Logging();

    @NotNull(message = "Конфигурация маскирования данных не может быть null")
    private DataMasking dataMasking = new DataMasking();

    @Getter
    @Setter
    public static class Jwt {
        @NotBlank(message = "Секретный ключ JWT не может быть пустым")
        private String secret = "k3J8mN2xP9qR7vT1yU4iO0pL6sA5dF2gH9jK3lM8nB7vC1xZ0qW4eR9tY6uI";

        @Positive(message = "Время жизни JWT токена должно быть положительным")
        private long expiration = 86400000;

        private String issuer = "bank-cards-app";

        @NotBlank(message = "Аудитория JWT не может быть пустой")
        private String audience = "bank-cards-users";
    }

    @Getter
    @Setter
    public static class Cors {
        private String allowedOrigins = "http://localhost:3000,http://localhost:8080";

        private String allowedMethods = "GET,POST,PUT,PATCH,DELETE,OPTIONS";

        private String allowedHeaders = "Authorization,Content-Type,X-Requested-With,Accept,Origin," +
                "Access-Control-Request-Method,Access-Control-Request-Headers";

        private String exposedHeaders = "Authorization,X-Total-Count,X-Alert-Message";

        private boolean allowCredentials = true;

        @Positive(message = "Максимальное время CORS должно быть положительным")
        private long maxAge = 3600;
    }

    @Getter
    @Setter
    public static class Bank {
        @Positive(message = "Комиссия за перевод должна быть положительной")
        private double transferCommissionRate = 0.01;

        @NotBlank(message = "Валюта по умолчанию не может быть пустой")
        private String defaultCurrency = "RUB";

        @Positive(message = "Лимит кредитной карты должен быть положительным")
        private double creditCardLimit = 50000.0;

        @Positive(message = "Максимальная сумма перевода должна быть положительной")
        private double maxTransferAmount = 1000000.0;

        @Positive(message = "Минимальная сумма перевода должна быть положительной")
        private double minTransferAmount = 0.01;

        private int cardExpiryYears = 4;

        private boolean allowInternationalTransfers = false;

        private int cardNumberLength = 16;

        private int cvvLength = 3;
    }

    @Getter
    @Setter
    public static class Security {
        @Min(value = 8, message = "Минимальная длина пароля должна быть не менее 8 символов")
        private int passwordMinLength = 8;

        private boolean requireUppercase = true;

        private boolean requireLowercase = true;

        private boolean requireDigits = true;

        private boolean requireSpecialChars = true;

        private int maxLoginAttempts = 5;

        private int accountLockoutDuration = 30;

        private boolean enableHttps = false;

        private boolean enableCsrf = false;

        private String[] publicEndpoints = {
                "/api/auth/**",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/swagger-resources/**",
                "/webjars/**"
        };
    }

    @Getter
    @Setter
    public static class Api {
        private String version = "1.0.0";

        private String title = "API для управления банковскими картами";

        private String description = "REST API для управления банковскими картами и транзакциями";

        private String contactName = "Служба поддержки банковских карт";

        private String contactEmail = "support@bankcards.com";

        private String contactUrl = "https://bankcards.com";

        private String license = "Apache 2.0";

        private String licenseUrl = "https://www.apache.org/licenses/LICENSE-2.0.html";

        private int defaultPageSize = 20;

        private int maxPageSize = 100;

        private String basePath = "/api";
    }

    @Getter
    @Setter
    public static class Logging {
        private boolean enableRequestLogging = true;

        private boolean enableSqlLogging = false;

        private boolean enablePerformanceLogging = false;

        private String logLevel = "INFO";

        private String logPath = "./logs";

        private int maxFileSize = 10;

        private int maxHistory = 30;

        private boolean logMaskedData = true;
    }

    @Getter
    @Setter
    public static class DataMasking {
        private String cardNumberMask = "**** **** **** ####";

        private int visibleCardDigits = 4;

        private boolean maskCvv = true;

        private boolean maskOwnerName = false;

        private String phoneMask = "*** *** ## ##";
    }
}