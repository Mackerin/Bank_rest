package com.example.bankcards.testConstants;

public class MessageTestConstants {

    // Аутентификация
    public static final String INVALID_CREDENTIALS_MESSAGE = "Неверные учетные данные";
    public static final String INVALID_CURRENT_PASSWORD_MESSAGE = "Текущий пароль неверен";
    public static final String PASSWORD_CANNOT_BE_NULL = "Пароль не может быть null";

    // Транзакции
    public static final String TEST_TRANSACTION_DESCRIPTION = "Тестовая транзакция";
    public static final String TEST_TRANSFER_DESCRIPTION = "Тестовый перевод";
    public static final String TEST_OWN_CARDS_TRANSFER_DESCRIPTION = "Перевод между своими картами";

    // Сущности
    public static final String USER_NOT_FOUND_MESSAGE = "User не найден(a) с id: '1'";
    public static final String CARD_NOT_FOUND_MESSAGE = "Карта не найден(a) с id: '999'";
    public static final String USER_NOT_FOUND_BY_ID = "Пользователь не найден(a) с id: '999'";
    public static final String USER_NOT_FOUND_BY_USERNAME = "Пользователь не найден(a) с username: 'nonexistent'";
}