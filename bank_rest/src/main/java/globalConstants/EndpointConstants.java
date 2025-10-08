package globalConstants;

public class EndpointConstants {

    // Аутентификация
    public static final String LOGIN_ENDPOINT = "/api/auth/login";
    public static final String REFRESH_ENDPOINT = "/api/auth/refresh";
    public static final String VALIDATE_ENDPOINT = "/api/auth/validate";
    public static final String CHANGE_PASSWORD_ENDPOINT = "/api/auth/change-password";
    public static final String LOGOUT_ENDPOINT = "/api/auth/logout";

    // Карты
    public static final String CARDS_BASE_PATH = "/api/cards";
    public static final String SEARCH_CARDS_ENDPOINT = CARDS_BASE_PATH + "/search";
    public static final String CARD_BY_ID_ENDPOINT = CARDS_BASE_PATH + "/1";
    public static final String CARD_STATUS_ENDPOINT = CARDS_BASE_PATH + "/1/status";
    public static final String USER_CARDS_BY_ID_ENDPOINT = CARDS_BASE_PATH + "/user/2";
    public static final String BLOCK_CARD_ENDPOINT = CARDS_BASE_PATH + "/1/block";
    public static final String UNBLOCK_CARD_ENDPOINT = CARDS_BASE_PATH + "/1/unblock";
    public static final String CARD_BALANCE_ENDPOINT = CARDS_BASE_PATH + "/1/balance";
    public static final String TOTAL_BALANCE_ENDPOINT = CARDS_BASE_PATH + "/total-balance";
    public static final String VALIDATE_CARD_ENDPOINT = CARDS_BASE_PATH + "/1/validate";

    // Транзакции
    public static final String GET_TRANSACTIONS_ENDPOINT = "/api/transactions";
    public static final String TRANSFER_ENDPOINT = GET_TRANSACTIONS_ENDPOINT + "/transfer";
    public static final String TRANSFER_BETWEEN_OWN_CARDS_ENDPOINT = TRANSFER_ENDPOINT + "-between-own-cards";
    public static final String DEPOSIT_ENDPOINT = GET_TRANSACTIONS_ENDPOINT + "/deposit";
    public static final String GET_TRANSACTION_BY_ID_ENDPOINT = GET_TRANSACTIONS_ENDPOINT + "/1";
    public static final String GET_TRANSACTION_BY_TRANSACTION_ID_ENDPOINT = GET_TRANSACTIONS_ENDPOINT
            + "/transaction-id/TXN123";
    public static final String GET_USER_TRANSACTIONS_ENDPOINT = GET_TRANSACTIONS_ENDPOINT + "/user/2";
    public static final String CANCEL_TRANSACTION_ENDPOINT = GET_TRANSACTIONS_ENDPOINT + "/1/cancel";

    // Пользователи
    public static final String USERS_BASE_PATH = "/api/users";
    public static final String USERS_PATH_BY_ID = USERS_BASE_PATH + "/1";
    public static final String USERS_ACTIVATE_ENDPOINT = USERS_PATH_BY_ID + "/activate";
    public static final String USERS_OWN_PATH = USERS_BASE_PATH + "/me";
    public static final String USERS_PATH_BY_ROLE = USERS_BASE_PATH + "/role/ROLE_USER";
    public static final String USERS_PATH_BY_USERNAME = USERS_BASE_PATH + "/username/testuser";
}