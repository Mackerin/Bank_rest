package globalConstants;

public class MessageConstants {

    // Аутентификация
    public static final String LOGIN_SUCCESS_MESSAGE = "Успешный вход в систему";
    public static final String REFRESH_SUCCESS_MESSAGE = "Токен успешно обновлен";
    public static final String VALIDATE_SUCCESS_MESSAGE = "Токен действителен";
    public static final String INVALID_TOKEN_MESSAGE = "Токен недействителен";
    public static final String CHANGE_PASSWORD_SUCCESS_MESSAGE = "Пароль успешно изменен";
    public static final String LOGOUT_SUCCESS_MESSAGE = "Успешный выход из системы";
    public static final String AUTHORIZATION_HEADER_INVALID_FORMAT = "Неверный формат заголовка Authorization";
    public static final String AUTH_INVALID_CREDENTIALS_MESSAGE = "Неверное имя пользователя или пароль";
    public static final String AUTH_CURRENT_PASSWORD_INCORRECT_MESSAGE = "Текущий пароль неверен";
    public static final String AUTH_NEW_PASSWORD_TOO_SHORT_MESSAGE = "Новый пароль должен содержать минимум 8 символов";
    public static final String AUTH_INVALID_TOKEN_MESSAGE = "Недействительный токен";

    // Карты
    public static final String GET_CARD_STATUS_MESSAGE = "Статус карты успешно получен";
    public static final String CARD_CREATED_SUCCESS = "Карта успешно создана";
    public static final String CARD_BLOCKED_SUCCESS = "Карта успешно заблокирована";
    public static final String CARD_UNBLOCKED_SUCCESS = "Карта успешно разблокирована";
    public static final String CARD_DEACTIVATED_SUCCESS = "Карта успешно деактивирована";
    public static final String CARD_VALID_SUCCESS = "Карта действительна";
    public static final String GET_CARD_BY_ID_SUCCESS_MESSAGE = "Карта успешно получена";
    public static final String GET_USERS_CARDS_SUCCESS_MESSAGE = "Карты пользователя успешно получены";
    public static final String GET_BALANCE_SUCCESS_MESSAGE = "Баланс успешно получен";
    public static final String GET_TOTAL_BALANCE_SUCCESS_MESSAGE = "Общий баланс успешно получен";
    public static final String CARD_ALREADY_BLOCKED_MESSAGE = "Карта уже заблокирована!";
    public static final String CANNOT_DEACTIVATE_WITH_BALANCE_MESSAGE =
            "Нельзя деактивировать карту с положительным балансом!";

    // Транзакции
    public static final String TRANSFER_SUCCESS_MESSAGE = "Перевод успешно выполнен";
    public static final String TRANSFER_BETWEEN_OWN_CARDS_SUCCESS_MESSAGE =
            "Перевод между своими картами успешно выполнен";
    public static final String DEPOSIT_AMOUNT_MUST_BE_POSITIVE_MESSAGE = "Сумма пополнения должна быть положительной";
    public static final String DEPOSIT_SUCCESS_MESSAGE = "Счет успешно пополнен";
    public static final String DEPOSIT_TO_CARD_MESSAGE = "Пополнение карты";
    public static final String GET_TRANSACTIONS_SUCCESS_MESSAGE = "История транзакций успешно получена";
    public static final String GET_TRANSACTION_SUCCESS_MESSAGE = "Транзакция успешно получена";
    public static final String GET_USER_TRANSACTIONS_SUCCESS_MESSAGE = "Транзакции пользователя успешно получены";
    public static final String CANCEL_TRANSACTION_SUCCESS_MESSAGE = "Транзакция успешно отменена";
    public static final String ONLY_PENDING_TRANSACTIONS_CAN_BE_CANCELLED_MESSAGE =
            "Отменить можно только транзакции в статусе ожидания";
    public static final String BOTH_CARDS_MUST_BELONG_TO_CURRENT_USER_MESSAGE =
            "Обе карты должны принадлежать текущему пользователю";
    public static final String SOURCE_CARD_INVALID_OR_EXPIRED_MESSAGE =
            "Карта отправителя недействительна или просрочена";
    public static final String CANNOT_TRANSFER_TO_SAME_CARD_MESSAGE = "Нельзя переводить на ту же карту";
    public static final String TRANSFER_AMOUNT_MUST_BE_POSITIVE_MESSAGE = "Сумма перевода должна быть положительной";
    public static final String INSUFFICIENT_FUNDS_MESSAGE = "Недостаточно средств";

    // Пользователи
    public static final String CREATE_USER_SUCCESS_MESSAGE = "Пользователь успешно создан";
    public static final String GET_USERS_SUCCESS_MESSAGE = "Пользователи успешно получены";
    public static final String GET_USER_SUCCESS_MESSAGE = "Пользователь успешно получен";
    public static final String UPDATE_USER_SUCCESS_MESSAGE = "Пользователь успешно обновлен";
    public static final String DELETE_USER_SUCCESS_MESSAGE = "Пользователь успешно удален";
    public static final String ACTIVATE_USER_SUCCESS_MESSAGE = "Пользователь успешно активирован";
    public static final String GET_CURRENT_USER_SUCCESS_MESSAGE = "Текущий пользователь успешно получен";
    public static final String USERNAME_ALREADY_EXISTS = "Пользователь уже зарегистрирован";
    public static final String EMAIL_ALREADY_EXISTS = "Почта уже зарегистрирована";
}