package com.example.bankcards.testConstants;

import java.math.BigDecimal;

public class UserTestConstants {

    // Пользовательские данные
    public static final String TEST_USERNAME = "testuser";
    public static final String NON_EXIST_USERNAME = "nonexistent";
    public static final String TEST_ADMIN = "admin";
    public static final String TEST_PASSWORD = "password123";
    public static final String ENCODE_PASSWORD = "encodedPassword";
    public static final String NEW_ENCODE_PASSWORD = "newEncodedPassword";
    public static final String OLD_PASSWORD = "oldPassword";
    public static final String NEW_PASSWORD = "newPassword";
    public static final String WRONG_PASSWORD = "wrongPassword";

    // Личные данные
    public static final String TEST_FIRST_NAME = "Test";
    public static final String TEST_LAST_NAME = "User";
    public static final String UPDATE_FIRST_NAME = "Updated";
    public static final String UPDATE_LAST_NAME = "User";
    public static final String TEST_EMAIL = "test@example.com";
    public static final String UPDATE_EMAIL = "updated@example.com";
    public static final String TEST_PHONE_NUMBER = "+1234567890";
    public static final String UPDATE_PHONE_NUMBER = "+1111111111";

    // Роли
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";

    // Данные карты
    public static final String CARD_NUMBER = "1234567890123456";
    public static final String CARD_NUMBER_MASKED = "**** **** **** 3456";
    public static final String ENCRYPTED_CARD_NUMBER = "encrypted123";
    public static final BigDecimal CARD_BALANCE = new BigDecimal("1000.00");
    public static final BigDecimal TOTAL_CARD_BALANCE = new BigDecimal("2500.50");

    // Сортировка
    public static final String SORT_CREATED_AT_DESC = "createdAt,desc";
}