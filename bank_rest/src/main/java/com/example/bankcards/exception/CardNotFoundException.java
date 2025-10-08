package com.example.bankcards.exception;

public class CardNotFoundException extends ResourceNotFoundException {

    public CardNotFoundException(Long cardId) {
        super("Карта", "id", cardId);
    }

    public CardNotFoundException(String cardNumber) {
        super("Карта", "номер", cardNumber);
    }
}
