package com.example.bankcards.service;

import com.example.bankcards.dto.OwnCardsTransferRequest;
import com.example.bankcards.dto.TransactionDTO;
import com.example.bankcards.dto.TransactionRequest;
import com.example.bankcards.entity.*;
import com.example.bankcards.exception.*;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.util.DataMasker;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static globalConstants.MessageConstants.*;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final DataMasker dataMasker;
    @Value("${app.bank.transfer-commission-rate:0.01}")
    private BigDecimal commissionRate;

    public TransactionDTO transferMoney(TransactionRequest request) {
        Card fromCard = cardRepository.findById(request.getFromCardId())
                .orElseThrow(() -> new ResourceNotFoundException("Card", "id", request.getFromCardId().toString()));

        Card toCard = cardRepository.findByCardNumber(request.getToCardNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Card", "cardNumber", request.getToCardNumber()));

        validateTransfer(fromCard, toCard, request.getAmount());

        BigDecimal commission = calculateCommission(request.getAmount());
        BigDecimal totalAmount = request.getAmount().add(commission);

        Transaction transaction = createTransaction(fromCard, toCard, request.getAmount(),
                commission, request.getDescription());

        try {
            performTransfer(fromCard, toCard, request.getAmount(), commission);

            transaction.setStatus(TransactionStatus.COMPLETED);
            Transaction savedTransaction = transactionRepository.save(transaction);

            return convertToDTO(savedTransaction);

        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw new TransactionException("Перевод не прошел: " + e.getMessage(), e);
        }
    }

    public TransactionDTO depositMoney(Long cardId, BigDecimal amount) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "id", cardId.toString()));

        validateDeposit(amount);

        Transaction transaction = new Transaction();
        transaction.setTransactionId(generateTransactionId());
        transaction.setAmount(amount);
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setDescription(DEPOSIT_TO_CARD_MESSAGE);
        transaction.setCommission(BigDecimal.ZERO);
        transaction.setToCard(card);

        try {

            card.setBalance(card.getBalance().add(amount));
            cardRepository.save(card);

            transaction.setStatus(TransactionStatus.COMPLETED);
            Transaction savedTransaction = transactionRepository.save(transaction);

            return convertToDTO(savedTransaction);

        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw new TransactionException("Пополнение не прошло: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<TransactionDTO> getUserTransactions(Long userId) {
        return transactionRepository.findAllUserTransactions(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TransactionDTO getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));
        return convertToDTO(transaction);
    }

    @Transactional(readOnly = true)
    public TransactionDTO getTransactionByTransactionId(String transactionId) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));
        return convertToDTO(transaction);
    }

    public void cancelTransaction(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new TransactionException(ONLY_PENDING_TRANSACTIONS_CAN_BE_CANCELLED_MESSAGE + "Текущий статус: "
                    + transaction.getStatus());
        }

        transaction.setStatus(TransactionStatus.CANCELLED);
        transactionRepository.save(transaction);
    }

    private Transaction createTransaction(Card fromCard, Card toCard, BigDecimal amount,
                                          BigDecimal commission, String description) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(generateTransactionId());
        transaction.setAmount(amount);
        transaction.setType(TransactionType.TRANSFER);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setDescription(description != null ? description : "Перевод между картами");
        transaction.setCommission(commission);
        transaction.setFromCard(fromCard);
        transaction.setToCard(toCard);
        return transaction;
    }

    public TransactionDTO transferBetweenOwnCards(OwnCardsTransferRequest request, Long userId) {
        Card fromCard = cardRepository.findById(request.getFromCardId())
                .orElseThrow(() -> new ResourceNotFoundException("Card", "id", request.getFromCardId().toString()));
        Card toCard = cardRepository.findById(request.getToCardId())
                .orElseThrow(() -> new ResourceNotFoundException("Card", "id", request.getToCardId().toString()));
        if (!fromCard.getUser().getId().equals(userId) || !toCard.getUser().getId().equals(userId)) {
            throw new CardOperationException(BOTH_CARDS_MUST_BELONG_TO_CURRENT_USER_MESSAGE);
        }
        validateTransfer(fromCard, toCard, request.getAmount());
        BigDecimal commission = calculateCommission(request.getAmount());
        Transaction transaction = createTransaction(fromCard, toCard, request.getAmount(),
                commission, request.getDescription());
        try {
            performTransfer(fromCard, toCard, request.getAmount(), commission);
            transaction.setStatus(TransactionStatus.COMPLETED);
            Transaction savedTransaction = transactionRepository.save(transaction);
            return convertToDTO(savedTransaction);
        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw new TransactionException("Перевод не прошел: " + e.getMessage(), e);
        }
    }

    private BigDecimal calculateCommission(BigDecimal amount) {
        return amount.multiply(commissionRate)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void validateTransfer(Card fromCard, Card toCard, BigDecimal amount) {
        if (!fromCard.isValid()) {
            throw new CardOperationException(SOURCE_CARD_INVALID_OR_EXPIRED_MESSAGE);
        }
        if (!toCard.isValid()) {
            throw new CardOperationException("Карта получателя недействительна или просрочена");
        }
        if (fromCard.equals(toCard)) {
            throw new ValidationException(CANNOT_TRANSFER_TO_SAME_CARD_MESSAGE);
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(TRANSFER_AMOUNT_MUST_BE_POSITIVE_MESSAGE);
        }
        if (amount.compareTo(new BigDecimal("0.01")) < 0) {
            throw new ValidationException("Минимальная сумма перевода — 0.01");
        }
        BigDecimal commission = calculateCommission(amount);
        BigDecimal totalAmount = amount.add(commission);
        if (!hasSufficientFunds(fromCard, totalAmount)) {
            throw new InsufficientFundsException(
                    String.format(INSUFFICIENT_FUNDS_MESSAGE + " Требуется: %s, Доступно: %s",
                            totalAmount,
                            fromCard.getCardType() == CardType.DEBIT ? fromCard.getBalance() : getAvailableCredit(fromCard))
            );
        }
    }

    private boolean hasSufficientFunds(Card card, BigDecimal amount) {
        if (card.getCardType() == CardType.DEBIT) {
            return card.getBalance().compareTo(amount) >= 0;
        } else {
            return getAvailableCredit(card).compareTo(amount) >= 0;
        }
    }

    private BigDecimal getAvailableCredit(Card card) {
        if (card.getCardType() == CardType.CREDIT) {
            BigDecimal usedCredit = card.getBalance().compareTo(BigDecimal.ZERO) < 0 ?
                    card.getBalance().abs() : BigDecimal.ZERO;
            return card.getCreditLimit().subtract(usedCredit);
        }
        return BigDecimal.ZERO;
    }

    private void validateDeposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(DEPOSIT_AMOUNT_MUST_BE_POSITIVE_MESSAGE);
        }
        if (amount.compareTo(new BigDecimal("0.01")) < 0) {
            throw new ValidationException("Минимальная сумма пополнения — 0.01");
        }
    }

    private void performTransfer(Card fromCard, Card toCard, BigDecimal amount, BigDecimal commission) {
        BigDecimal totalAmount = amount.add(commission);
        if (fromCard.getCardType() == CardType.DEBIT) {
            fromCard.setBalance(fromCard.getBalance().subtract(totalAmount));
        } else {
            fromCard.setBalance(fromCard.getBalance().subtract(totalAmount));
        }
        toCard.setBalance(toCard.getBalance().add(amount));
        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }

    private String generateTransactionId() {
        return "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    private TransactionDTO convertToDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setTransactionId(transaction.getTransactionId());
        dto.setAmount(transaction.getAmount());
        dto.setType(transaction.getType());
        dto.setStatus(transaction.getStatus());
        dto.setDescription(transaction.getDescription());
        dto.setCommission(transaction.getCommission());
        dto.setCreatedAt(transaction.getCreatedAt());
        if (transaction.getFromCard() != null && transaction.getFromCard().getUser() != null) {
            User fromUser = transaction.getFromCard().getUser();
            dto.setFromUserId(fromUser.getId());
            dto.setFromUserFullName(fromUser.getFirstName() + " " + fromUser.getLastName());
            dto.setFromCardId(transaction.getFromCard().getId());
            dto.setFromCardNumber(dataMasker.maskCardNumber(transaction.getFromCard().getCardNumber()));
        }
        if (transaction.getToCard() != null && transaction.getToCard().getUser() != null) {
            User toUser = transaction.getToCard().getUser();
            dto.setToUserId(toUser.getId());
            dto.setToUserFullName(toUser.getFirstName() + " " + toUser.getLastName());
            dto.setToCardId(transaction.getToCard().getId());
            dto.setToCardNumber(dataMasker.maskCardNumber(transaction.getToCard().getCardNumber()));
        }
        return dto;
    }
}