package com.example.bankcards.security;

import com.example.bankcards.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("transactionSecurity")
@RequiredArgsConstructor
public class TransactionSecurity {

    private final TransactionRepository transactionRepository;

    public boolean isTransactionParticipant(Long transactionId, Long userId) {
        return transactionRepository.findById(transactionId)
                .map(transaction ->
                        (transaction.getFromCard() != null &&
                                transaction.getFromCard().getUser() != null &&
                                transaction.getFromCard().getUser().getId().equals(userId)) ||
                                (transaction.getToCard() != null &&
                                        transaction.getToCard().getUser() != null &&
                                        transaction.getToCard().getUser().getId().equals(userId))
                )
                .orElse(false);
    }

    public boolean isTransactionParticipantByTransactionId(String transactionId, Long userId) {
        return transactionRepository.findByTransactionId(transactionId)
                .map(transaction ->
                        (transaction.getFromCard() != null &&
                                transaction.getFromCard().getUser() != null &&
                                transaction.getFromCard().getUser().getId().equals(userId)) ||
                                (transaction.getToCard() != null &&
                                        transaction.getToCard().getUser() != null &&
                                        transaction.getToCard().getUser().getId().equals(userId))
                )
                .orElse(false);
    }

    public boolean isTransactionInitiator(Long transactionId, Long userId) {
        return transactionRepository.findById(transactionId)
                .map(transaction ->
                        transaction.getFromCard() != null &&
                                transaction.getFromCard().getUser() != null &&
                                transaction.getFromCard().getUser().getId().equals(userId)
                )
                .orElse(false);
    }
}