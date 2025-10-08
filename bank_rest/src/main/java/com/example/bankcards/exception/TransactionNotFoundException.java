package com.example.bankcards.exception;

public class TransactionNotFoundException extends ResourceNotFoundException {

    public TransactionNotFoundException(Long transactionId) {
        super("Транзакция", "id", transactionId);
    }

    public TransactionNotFoundException(String transactionNumber) {
        super("Транзакция", "номер", transactionNumber);
    }
}