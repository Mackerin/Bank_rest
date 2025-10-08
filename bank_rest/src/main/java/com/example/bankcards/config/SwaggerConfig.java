package com.example.bankcards.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

    private final ApplicationProperties applicationProperties;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("/").description("URL сервера по умолчанию")
                ))
                .info(new Info()
                        .title(applicationProperties.getApi().getTitle())
                        .description("""
                                REST API для системы управления банковскими картами и транзакциями.
                                                            
                                ## Основные возможности:
                                - Управление пользователями и аутентификация
                                - Создание и управление банковскими картами
                                - Выполнение денежных переводов и транзакций
                                - Безопасность на основе JWT токенов
                                - Маскирование конфиденциальных данных
                                                            
                                ## Роли и доступ:
                                - **ADMIN**: Полный доступ ко всем операциям
                                - **USER**: Доступ только к своим картам и операциям
                                                            
                                ## Авторизация:
                                Для доступа к защищенным endpoints необходимо:
                                1. Вызвать `/api/auth/login` для получения JWT токена
                                2. Добавить заголовок: `Authorization: Bearer <your_token>`
                                """)
                        .version(applicationProperties.getApi().getVersion())
                        .contact(new Contact()
                                .name(applicationProperties.getApi().getContactName())
                                .email(applicationProperties.getApi().getContactEmail())
                                .url(applicationProperties.getApi().getContactUrl()))
                        .license(new License()
                                .name(applicationProperties.getApi().getLicense())
                                .url(applicationProperties.getApi().getLicenseUrl())))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Введите JWT токен полученный при аутентификации")));
    }
}