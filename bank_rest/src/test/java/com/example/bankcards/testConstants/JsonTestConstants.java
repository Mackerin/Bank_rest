package com.example.bankcards.testConstants;

public class JsonTestConstants {

    // Общие поля ответа
    public static final String JSON_PATH_SUCCESS = "$.success";
    public static final String JSON_PATH_MESSAGE = "$.message";
    public static final String JSON_PATH_DATA = "$.data";

    // Поля внутри объекта
    public static final String JSON_PATH_DATA_TOKEN = JSON_PATH_DATA + ".token";
    public static final String JSON_PATH_DATA_ID = JSON_PATH_DATA + ".id";
    public static final String JSON_PATH_DATA_STATUS = JSON_PATH_DATA + ".status";
    public static final String JSON_PATH_DATA_BALANCE = JSON_PATH_DATA + ".balance";
    public static final String JSON_PATH_DATA_CURRENCY = JSON_PATH_DATA + ".currency";
    public static final String JSON_PATH_DATA_TRANSACTION_ID = JSON_PATH_DATA + ".transactionId";
    public static final String JSON_PATH_DATA_USERNAME = JSON_PATH_DATA + ".username";

    // Поля в массивах
    public static final String JSON_PATH_DATA_ARRAY_ID = "$.data[0].id";
    public static final String JSON_PATH_DATA_ARRAY_ROLE = "$.data[0].role";

    // Поля в пагинированных или вложенных структурах
    public static final String JSON_PATH_DATA_CONTENT_ID = JSON_PATH_DATA + ".content[0].id";
}