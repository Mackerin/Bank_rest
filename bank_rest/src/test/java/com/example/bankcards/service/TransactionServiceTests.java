package com.example.bankcards.service;

import com.example.bankcards.testConstants.*;
import com.example.bankcards.dto.OwnCardsTransferRequest;
import com.example.bankcards.dto.TransactionDTO;
import com.example.bankcards.dto.TransactionRequest;
import com.example.bankcards.entity.*;
import com.example.bankcards.exception.*;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.util.DataMasker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.example.bankcards.testConstants.AnotherUserTestConstants.*;
import static com.example.bankcards.testConstants.TransactionTestConstants.*;
import static com.example.bankcards.testConstants.UserTestConstants.CARD_BALANCE;
import static globalConstants.MessageConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Transaction Service tests")
class TransactionServiceTests {
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private DataMasker dataMasker;
    @InjectMocks
    private TransactionService transactionService;
    private User testUser;
    private User toUser;
    private Card fromCard;
    private Card toCard;
    private Card creditCard;
    private Transaction testTransaction;
    private TransactionRequest transactionRequest;
    private OwnCardsTransferRequest ownCardsTransferRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName(UserTestConstants.TEST_FIRST_NAME)
                .lastName(UserTestConstants.TEST_LAST_NAME)
                .username(UserTestConstants.TEST_USERNAME)
                .build();
        toUser = User.builder()
                .id(2L)
                .firstName(ANOTHER_FIRST_NAME)
                .lastName(ANOTHER_LAST_NAME)
                .username(ANOTHER_USERNAME)
                .build();

        fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setCardNumber(TEST_FROM_CARD_NUMBER);
        fromCard.setCardHolderName(TransactionTestConstants.TEST_FROM_USER_FULL_NAME);
        fromCard.setExpiryDate(LocalDateTime.now().plusYears(2).toLocalDate());
        fromCard.setCardType(CardType.DEBIT);
        fromCard.setCurrency(Currency.RUB);
        fromCard.setBalance(CARD_BALANCE);
        fromCard.setActive(true);
        fromCard.setIsBlocked(false);
        fromCard.setUser(testUser);

        toCard = new Card();
        toCard.setId(2L);
        toCard.setCardNumber(TEST_TO_CARD_NUMBER);
        toCard.setCardHolderName(TransactionTestConstants.TEST_TO_USER_FULL_NAME);
        toCard.setExpiryDate(LocalDateTime.now().plusYears(3).toLocalDate());
        toCard.setCardType(CardType.DEBIT);
        toCard.setCurrency(Currency.RUB);
        toCard.setBalance(TO_CARD_BALANCE);
        toCard.setActive(true);
        toCard.setIsBlocked(false);
        toCard.setUser(toUser);

        creditCard = new Card();
        creditCard.setId(3L);
        creditCard.setCardNumber(CREDIT_CARD_NUMBER);
        creditCard.setCardHolderName(TransactionTestConstants.TEST_FROM_USER_FULL_NAME);
        creditCard.setExpiryDate(LocalDateTime.now().plusYears(2).toLocalDate());
        creditCard.setCardType(CardType.CREDIT);
        creditCard.setCurrency(Currency.RUB);
        creditCard.setBalance(BigDecimal.ZERO);
        creditCard.setCreditLimit(CREDIT_CARD_LIMIT);
        creditCard.setActive(true);
        creditCard.setIsBlocked(false);
        creditCard.setUser(testUser);

        transactionRequest = new TransactionRequest();
        transactionRequest.setFromCardId(1L);
        transactionRequest.setToCardNumber(TEST_TO_CARD_NUMBER);
        transactionRequest.setAmount(new BigDecimal("100.00"));
        transactionRequest.setDescription(MessageTestConstants.TEST_TRANSFER_DESCRIPTION);

        ownCardsTransferRequest = new OwnCardsTransferRequest();
        ownCardsTransferRequest.setFromCardId(1L);
        ownCardsTransferRequest.setToCardId(3L);
        ownCardsTransferRequest.setAmount(new BigDecimal("50.00"));
        ownCardsTransferRequest.setDescription(MessageTestConstants.TEST_OWN_CARDS_TRANSFER_DESCRIPTION);

        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setTransactionId(TransactionTestConstants.TRANSACTION_ID);
        testTransaction.setAmount(new BigDecimal("100.00"));
        testTransaction.setType(TransactionType.TRANSFER);
        testTransaction.setStatus(TransactionStatus.COMPLETED);
        testTransaction.setDescription(MessageTestConstants.TEST_TRANSFER_DESCRIPTION);
        testTransaction.setCommission(new BigDecimal("1.00"));
        testTransaction.setFromCard(fromCard);
        testTransaction.setToCard(toCard);
        testTransaction.setCreatedAt(LocalDateTime.now());
        ReflectionTestUtils.setField(transactionService, "commissionRate", new BigDecimal("0.01"));
    }

    @Test
    @DisplayName("Успешный перевод денег между картами")
    void transferMoneyWithValidRequestTest() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByCardNumber(TEST_TO_CARD_NUMBER)).thenReturn(Optional.of(toCard));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(dataMasker.maskCardNumber(TEST_FROM_CARD_NUMBER)).thenReturn(UserTestConstants.CARD_NUMBER_MASKED);
        when(dataMasker.maskCardNumber(TEST_TO_CARD_NUMBER)).thenReturn(UserTestConstants.CARD_NUMBER_MASKED);
        TransactionDTO result = transactionService.transferMoney(transactionRequest);
        assertNotNull(result);
        assertEquals(TransactionTestConstants.TRANSACTION_ID, result.getTransactionId());
        assertEquals(new BigDecimal("100.00"), result.getAmount());
        assertEquals(TransactionType.TRANSFER, result.getType());
        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        assertEquals(MessageTestConstants.TEST_TRANSFER_DESCRIPTION, result.getDescription());
        assertEquals(new BigDecimal("1.00"), result.getCommission());
        assertEquals(TransactionTestConstants.TEST_FROM_USER_FULL_NAME, result.getFromUserFullName());
        assertEquals(TransactionTestConstants.TEST_TO_USER_FULL_NAME, result.getToUserFullName());
        verify(cardRepository).findById(1L);
        verify(cardRepository).findByCardNumber(TEST_TO_CARD_NUMBER);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Перевод с несуществующей картой отправителя выбрасывает исключение")
    void transferMoneyWithNonExistentFromCardTest() {
        TransactionRequest invalidRequest = new TransactionRequest();
        invalidRequest.setFromCardId(999L);
        invalidRequest.setToCardNumber(TEST_TO_CARD_NUMBER);
        invalidRequest.setAmount(new BigDecimal("100.00"));
        invalidRequest.setDescription(MessageTestConstants.TEST_TRANSFER_DESCRIPTION);
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> transactionService.transferMoney(invalidRequest));
        assertTrue(exception.getMessage().contains("Card"));
        verify(cardRepository).findById(999L);
        verify(cardRepository, never()).findByCardNumber(anyString());
    }

    @Test
    @DisplayName("Перевод с несуществующей картой получателя выбрасывает исключение")
    void transferMoneyWithNonExistentToCardTest() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByCardNumber(TEST_TO_CARD_NUMBER)).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> transactionService.transferMoney(transactionRequest));
        assertTrue(exception.getMessage().contains("Card"));
        verify(cardRepository).findById(1L);
        verify(cardRepository).findByCardNumber(TEST_TO_CARD_NUMBER);
    }

    @Test
    @DisplayName("Перевод с недостаточными средствами выбрасывает исключение")
    void transferMoneyWithInsufficientFundsTest() {
        transactionRequest.setAmount(new BigDecimal("5000.00"));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByCardNumber(TEST_TO_CARD_NUMBER)).thenReturn(Optional.of(toCard));
        InsufficientFundsException exception = assertThrows(InsufficientFundsException.class,
                () -> transactionService.transferMoney(transactionRequest));
        assertTrue(exception.getMessage().contains(INSUFFICIENT_FUNDS_MESSAGE));
        verify(cardRepository).findById(1L);
        verify(cardRepository).findByCardNumber(TEST_TO_CARD_NUMBER);
    }

    @Test
    @DisplayName("Перевод с недействительной картой отправителя выбрасывает исключение")
    void transferMoneyWithInvalidFromCardTest() {
        fromCard.setActive(false);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByCardNumber(TEST_TO_CARD_NUMBER)).thenReturn(Optional.of(toCard));
        CardOperationException exception = assertThrows(CardOperationException.class,
                () -> transactionService.transferMoney(transactionRequest));
        assertTrue(exception.getMessage().contains(SOURCE_CARD_INVALID_OR_EXPIRED_MESSAGE));
        verify(cardRepository).findById(1L);
        verify(cardRepository).findByCardNumber(TEST_TO_CARD_NUMBER);
    }

    @Test
    @DisplayName("Успешное пополнение карты")
    void depositMoneyWithValidAmountTest() {
        BigDecimal depositAmount = new BigDecimal("200.00");
        Transaction depositTransaction = new Transaction();
        depositTransaction.setId(2L);
        depositTransaction.setTransactionId(TRANSACTION_ID);
        depositTransaction.setAmount(depositAmount);
        depositTransaction.setType(TransactionType.DEPOSIT);
        depositTransaction.setStatus(TransactionStatus.COMPLETED);
        depositTransaction.setDescription(DEPOSIT_TO_CARD_MESSAGE);
        depositTransaction.setCommission(BigDecimal.ZERO);
        depositTransaction.setToCard(fromCard);
        depositTransaction.setCreatedAt(LocalDateTime.now());
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(depositTransaction);
        when(dataMasker.maskCardNumber(TEST_FROM_CARD_NUMBER)).thenReturn(UserTestConstants.CARD_NUMBER_MASKED);
        TransactionDTO result = transactionService.depositMoney(1L, depositAmount);
        assertNotNull(result);
        assertEquals(TRANSACTION_ID, result.getTransactionId());
        assertEquals(depositAmount, result.getAmount());
        assertEquals(TransactionType.DEPOSIT, result.getType());
        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        assertEquals(BigDecimal.ZERO, result.getCommission());
        verify(cardRepository).findById(1L);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Пополнение с отрицательной суммой выбрасывает исключение")
    void depositMoneyWithNegativeAmountTest() {
        BigDecimal negativeAmount = new BigDecimal("-100.00");
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        ValidationException exception = assertThrows(ValidationException.class,
                () -> transactionService.depositMoney(1L, negativeAmount));
        assertTrue(exception.getMessage().contains(DEPOSIT_AMOUNT_MUST_BE_POSITIVE_MESSAGE));
        verify(cardRepository).findById(1L);
        verify(cardRepository, never()).save(any(Card.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Получение транзакций пользователя возвращает список")
    void getUserTransactionsTest() {
        when(transactionRepository.findAllUserTransactions(1L)).thenReturn(Arrays.asList(testTransaction));
        when(dataMasker.maskCardNumber(TEST_FROM_CARD_NUMBER)).thenReturn(UserTestConstants.CARD_NUMBER_MASKED);
        when(dataMasker.maskCardNumber(TEST_TO_CARD_NUMBER)).thenReturn(UserTestConstants.CARD_NUMBER_MASKED);
        List<TransactionDTO> result = transactionService.getUserTransactions(1L);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TransactionTestConstants.TRANSACTION_ID, result.get(0).getTransactionId());
        verify(transactionRepository).findAllUserTransactions(1L);
    }

    @Test
    @DisplayName("Получение транзакции по ID возвращает TransactionDTO")
    void getTransactionByIdWithValidIdTest() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(dataMasker.maskCardNumber(TEST_FROM_CARD_NUMBER)).thenReturn(UserTestConstants.CARD_NUMBER_MASKED);
        when(dataMasker.maskCardNumber(TEST_TO_CARD_NUMBER)).thenReturn(UserTestConstants.CARD_NUMBER_MASKED);
        TransactionDTO result = transactionService.getTransactionById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(TransactionTestConstants.TRANSACTION_ID, result.getTransactionId());
        verify(transactionRepository).findById(1L);
    }

    @Test
    @DisplayName("Получение несуществующей транзакции по ID выбрасывает исключение")
    void getTransactionByIdWithInvalidIdTest() {
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());
        TransactionNotFoundException exception = assertThrows(TransactionNotFoundException.class,
                () -> transactionService.getTransactionById(999L));
        assertTrue(exception.getMessage().contains("999"));
        verify(transactionRepository).findById(999L);
    }

    @Test
    @DisplayName("Получение транзакции по transactionId возвращает TransactionDTO")
    void getTransactionByTransactionIdWithValidIdTest() {
        when(transactionRepository.findByTransactionId(TransactionTestConstants.TRANSACTION_ID)).
                thenReturn(Optional.of(testTransaction));
        when(dataMasker.maskCardNumber(TEST_FROM_CARD_NUMBER)).thenReturn(UserTestConstants.CARD_NUMBER_MASKED);
        when(dataMasker.maskCardNumber(TEST_TO_CARD_NUMBER)).thenReturn(UserTestConstants.CARD_NUMBER_MASKED);
        TransactionDTO result = transactionService.getTransactionByTransactionId(TransactionTestConstants.TRANSACTION_ID);
        assertNotNull(result);
        assertEquals(TransactionTestConstants.TRANSACTION_ID, result.getTransactionId());
        verify(transactionRepository).findByTransactionId(TransactionTestConstants.TRANSACTION_ID);
    }

    @Test
    @DisplayName("Успешная отмена транзакции в режиме ожидания")
    void cancelPendingTransactionTest() {
        Transaction pendingTransaction = new Transaction();
        pendingTransaction.setId(1L);
        pendingTransaction.setStatus(TransactionStatus.PENDING);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(pendingTransaction));
        transactionService.cancelTransaction(1L);
        assertEquals(TransactionStatus.CANCELLED, pendingTransaction.getStatus());
        verify(transactionRepository).findById(1L);
        verify(transactionRepository).save(pendingTransaction);
    }

    @Test
    @DisplayName("Отмена завершенной транзакции выбрасывает исключение")
    void cancelCompletedTransactionTest() {
        Transaction completedTransaction = new Transaction();
        completedTransaction.setId(1L);
        completedTransaction.setStatus(TransactionStatus.COMPLETED);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(completedTransaction));
        TransactionException exception = assertThrows(TransactionException.class,
                () -> transactionService.cancelTransaction(1L));
        assertTrue(exception.getMessage().contains(ONLY_PENDING_TRANSACTIONS_CAN_BE_CANCELLED_MESSAGE));
        verify(transactionRepository).findById(1L);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Успешный перевод между собственными картами")
    void transferBetweenOwnCardsWithValidRequestTest() {
        testTransaction.setFromCard(fromCard);
        testTransaction.setToCard(creditCard);
        testTransaction.setAmount(new BigDecimal("50.00"));
        String fromCardNumber = fromCard.getCardNumber();
        String toCardNumber = creditCard.getCardNumber();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(3L)).thenReturn(Optional.of(creditCard));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(dataMasker.maskCardNumber(fromCardNumber)).thenReturn(UserTestConstants.CARD_NUMBER_MASKED);
        when(dataMasker.maskCardNumber(toCardNumber)).thenReturn(UserTestConstants.CARD_NUMBER_MASKED);
        TransactionDTO result = transactionService.transferBetweenOwnCards(ownCardsTransferRequest, 1L);
        assertNotNull(result);
        assertEquals(TransactionTestConstants.TRANSACTION_ID, result.getTransactionId());
        assertEquals(new BigDecimal("50.00"), result.getAmount());
        verify(cardRepository).findById(1L);
        verify(cardRepository).findById(3L);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Перевод между чужими картами выбрасывает исключение")
    void transferBetweenOwnCardsWithForeignCardTest() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        ownCardsTransferRequest.setToCardId(2L);
        CardOperationException exception = assertThrows(CardOperationException.class,
                () -> transactionService.transferBetweenOwnCards(ownCardsTransferRequest, 1L));
        assertTrue(exception.getMessage().contains(BOTH_CARDS_MUST_BELONG_TO_CURRENT_USER_MESSAGE));
        verify(cardRepository).findById(1L);
        verify(cardRepository).findById(2L);
    }

    @Test
    @DisplayName("Перевод с кредитной карты с достаточным кредитным лимитом")
    void transferFromCreditCardWithSufficientLimitTest() {
        transactionRequest.setFromCardId(3L);
        testTransaction.setFromCard(creditCard);
        testTransaction.setToCard(toCard);
        String fromCardNumber = creditCard.getCardNumber();
        String toCardNumber = toCard.getCardNumber();
        when(cardRepository.findById(3L)).thenReturn(Optional.of(creditCard));
        when(cardRepository.findByCardNumber(toCardNumber)).thenReturn(Optional.of(toCard));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(dataMasker.maskCardNumber(fromCardNumber)).thenReturn(UserTestConstants.CARD_NUMBER_MASKED);
        when(dataMasker.maskCardNumber(toCardNumber)).thenReturn(UserTestConstants.CARD_NUMBER_MASKED);
        TransactionDTO result = transactionService.transferMoney(transactionRequest);
        assertNotNull(result);
        assertEquals(TransactionTestConstants.TRANSACTION_ID, result.getTransactionId());
        verify(cardRepository).findById(3L);
        verify(cardRepository).findByCardNumber(toCardNumber);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Перевод с кредитной карты при недостаточном лимите выбрасывает исключение")
    void transferFromCreditCardWithInsufficientLimitTest() {
        // Карта с лимитом 1000, уже использовано 900, доступно 100
        Card creditCardLowLimit = new Card();
        creditCardLowLimit.setId(4L);
        creditCardLowLimit.setCardType(CardType.CREDIT);
        creditCardLowLimit.setCreditLimit(new BigDecimal("1000.00"));
        creditCardLowLimit.setBalance(new BigDecimal("-900.00"));
        creditCardLowLimit.setActive(true);
        creditCardLowLimit.setIsBlocked(false);
        creditCardLowLimit.setExpiryDate(LocalDate.now().plusYears(3));
        creditCardLowLimit.setUser(testUser);
        transactionRequest.setFromCardId(4L);
        transactionRequest.setAmount(new BigDecimal("150.00"));
        when(cardRepository.findById(4L)).thenReturn(Optional.of(creditCardLowLimit));
        when(cardRepository.findByCardNumber(TEST_TO_CARD_NUMBER)).thenReturn(Optional.of(toCard));
        InsufficientFundsException ex = assertThrows(InsufficientFundsException.class,
                () -> transactionService.transferMoney(transactionRequest));
        assertTrue(ex.getMessage().contains("Недостаточно средств"));
    }

    @Test
    @DisplayName("Перевод с нулевой суммой выбрасывает исключение")
    void transferWithZeroAmountTest() {
        transactionRequest.setAmount(BigDecimal.ZERO);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByCardNumber(TEST_TO_CARD_NUMBER)).thenReturn(Optional.of(toCard));
        ValidationException exception = assertThrows(ValidationException.class,
                () -> transactionService.transferMoney(transactionRequest));
        assertTrue(exception.getMessage().contains(TRANSFER_AMOUNT_MUST_BE_POSITIVE_MESSAGE));
    }

    @Test
    @DisplayName("Перевод на ту же самую карту выбрасывает исключение")
    void transferToSameCardTest() {
        transactionRequest.setToCardNumber(TEST_FROM_CARD_NUMBER);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByCardNumber(TEST_FROM_CARD_NUMBER)).thenReturn(Optional.of(fromCard));
        ValidationException exception = assertThrows(ValidationException.class,
                () -> transactionService.transferMoney(transactionRequest));
        assertTrue(exception.getMessage().contains(CANNOT_TRANSFER_TO_SAME_CARD_MESSAGE));
    }
}