package com.example.bankcards.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Slf4j
@Component
public class DataMasker {

    private static final Pattern DIGITS_ONLY = Pattern.compile("\\d+");
    private static final int DEFAULT_VISIBLE_CARD_DIGITS = 4;
    private static final int MIN_CARD_LENGTH = 13;
    private static final int MAX_CARD_LENGTH = 19;

    /**
     * Маскирует номер карты, оставляя только последние 4 цифры
     */
    public String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < MIN_CARD_LENGTH || cardNumber.length() > MAX_CARD_LENGTH) {
            log.warn("Недопустимая длина номера карты для маскирования: {}", cardNumber);
            return cardNumber;
        }
        if (!DIGITS_ONLY.matcher(cardNumber).matches()) {
            log.warn("Номер карты содержит нечисловые символы: {}", cardNumber);
            return cardNumber;
        }
        int totalLength = cardNumber.length();
        int maskedLength = totalLength - DEFAULT_VISIBLE_CARD_DIGITS;
        String maskedPart = "*".repeat(maskedLength);
        String visiblePart = cardNumber.substring(totalLength - DEFAULT_VISIBLE_CARD_DIGITS);
        return formatCardNumber(maskedPart + visiblePart);
    }

    /**
     * Форматирует номер карты с пробелами (XXXX XXXX XXXX XXXX)
     */
    public String formatCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < MIN_CARD_LENGTH) {
            return cardNumber;
        }
        String cleanNumber = cardNumber.replaceAll("\\D", "");
        return cleanNumber.replaceAll("(.{4})", "$1 ").trim();
    }
}