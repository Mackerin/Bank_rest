package com.example.bankcards.service;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.CardSearchRequest;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.dto.PageResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardType;
import com.example.bankcards.entity.Currency;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.CardOperationException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.DataEncryptor;
import com.example.bankcards.util.DataMasker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.example.bankcards.testConstants.MessageTestConstants.*;
import static com.example.bankcards.testConstants.UserTestConstants.*;
import static globalConstants.MessageConstants.CANNOT_DEACTIVATE_WITH_BALANCE_MESSAGE;
import static globalConstants.MessageConstants.CARD_ALREADY_BLOCKED_MESSAGE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Card Service tests")
class CardServiceTests {
    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DataMasker dataMasker;
    @Mock
    private DataEncryptor dataEncryptor;
    @InjectMocks
    private CardService cardService;
    private User testUser;
    private Card testCard;
    private CreateCardRequest createCardRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName(TEST_FIRST_NAME)
                .lastName(TEST_LAST_NAME)
                .build();
        testCard = new Card();
        testCard.setId(1L);
        testCard.setCardNumber(ENCRYPTED_CARD_NUMBER);
        testCard.setCardHolderName(TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        testCard.setExpiryDate(LocalDate.now().plusYears(4));
        testCard.setCardType(CardType.DEBIT);
        testCard.setCurrency(Currency.RUB);
        testCard.setBalance(BigDecimal.ZERO);
        testCard.setActive(true);
        testCard.setIsBlocked(false);
        testCard.setUser(testUser);
        createCardRequest = new CreateCardRequest();
        createCardRequest.setUserId(1L);
        createCardRequest.setCardType(CardType.DEBIT);
        createCardRequest.setCurrency(null);
        ReflectionTestUtils.setField(cardService, "defaultCurrency", "RUB");
    }

    @Test
    @DisplayName("Успешное создание дебетовой карты для существующего пользователя")
    void createCardWithValidRequestTest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(dataEncryptor.encrypt(anyString())).thenReturn("encryptedCardNumber");
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(dataEncryptor.decrypt(anyString())).thenReturn(CARD_NUMBER);
        when(dataMasker.maskCardNumber(CARD_NUMBER)).thenReturn(CARD_NUMBER_MASKED);
        CardDTO result = cardService.createCard(createCardRequest);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(CARD_NUMBER_MASKED, result.getCardNumber());
        assertEquals(CardType.DEBIT, result.getCardType());
        assertEquals(Currency.RUB, result.getCurrency());
        verify(userRepository).findById(1L);
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("Успешное создание кредитной карты")
    void createCreditCardWithLimitTest() {
        CreateCardRequest creditRequest = new CreateCardRequest();
        creditRequest.setUserId(1L);
        creditRequest.setCardType(CardType.CREDIT);
        creditRequest.setCurrency(Currency.USD);
        User user = User.builder()
                .id(1L)
                .firstName(TEST_FIRST_NAME)
                .lastName(TEST_LAST_NAME)
                .build();
        Card savedCard = new Card();
        savedCard.setId(1L);
        savedCard.setCardNumber(ENCRYPTED_CARD_NUMBER);
        savedCard.setCardHolderName(TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        savedCard.setExpiryDate(LocalDate.now().plusYears(4));
        savedCard.setCardType(CardType.CREDIT);
        savedCard.setCurrency(Currency.USD);
        savedCard.setBalance(BigDecimal.ZERO);
        savedCard.setActive(true);
        savedCard.setIsBlocked(false);
        savedCard.setUser(user);
        savedCard.setCreditLimit(new BigDecimal("10000.00")); // ← важно!
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(dataEncryptor.encrypt(anyString())).thenReturn(ENCRYPTED_CARD_NUMBER);
        when(cardRepository.save(any(Card.class))).thenReturn(savedCard);
        when(dataEncryptor.decrypt(ENCRYPTED_CARD_NUMBER)).thenReturn(CARD_NUMBER);
        when(dataMasker.maskCardNumber(CARD_NUMBER)).thenReturn(CARD_NUMBER_MASKED);
        ReflectionTestUtils.setField(cardService, "creditCardLimit", new BigDecimal("10000.00"));
        CardDTO result = cardService.createCard(creditRequest);
        assertNotNull(result.getCreditLimit());
        assertEquals(new BigDecimal("10000.00"), result.getCreditLimit());
    }

    @Test
    @DisplayName("Создание карты для несуществующего пользователя выбрасывает исключение")
    void createCardWithNonExistentUserTest() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> cardService.createCard(createCardRequest));
        assertEquals(USER_NOT_FOUND_MESSAGE, exception.getMessage());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    @DisplayName("Получение карты по ID для существующей карты")
    void getCardByIdWithValidIdTest() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(dataEncryptor.decrypt(ENCRYPTED_CARD_NUMBER)).thenReturn(CARD_NUMBER);
        when(dataMasker.maskCardNumber(CARD_NUMBER)).thenReturn(CARD_NUMBER_MASKED);
        CardDTO result = cardService.getCardById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(CARD_NUMBER_MASKED, result.getCardNumber());
        verify(cardRepository).findById(1L);
    }

    @Test
    @DisplayName("Получение несуществующей карты по ID выбрасывает CardNotFoundException")
    void getCardByIdWithInvalidIdTest() {
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());
        CardNotFoundException exception = assertThrows(CardNotFoundException.class,
                () -> cardService.getCardById(999L));
        assertEquals(CARD_NOT_FOUND_MESSAGE, exception.getMessage());
    }

    @Test
    @DisplayName("Получение активных карт пользователя")
    void getActiveUserCardsTest() {
        Card inactiveCard = new Card();
        inactiveCard.setActive(false);
        when(cardRepository.findByUserIdAndActiveTrue(1L)).thenReturn(List.of(testCard));
        when(dataEncryptor.decrypt(anyString())).thenReturn(CARD_NUMBER);
        when(dataMasker.maskCardNumber(anyString())).thenReturn(CARD_NUMBER_MASKED);
        List<CardDTO> result = cardService.getActiveUserCards(1L);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getActive());
    }

    @Test
    @DisplayName("Успешная блокировка активной и незаблокированной карты")
    void blockCardWithValidCardTest() {
        Card blockedCard = new Card();
        blockedCard.setId(testCard.getId());
        blockedCard.setCardNumber(testCard.getCardNumber());
        blockedCard.setCardHolderName(testCard.getCardHolderName());
        blockedCard.setExpiryDate(testCard.getExpiryDate());
        blockedCard.setCardType(testCard.getCardType());
        blockedCard.setCurrency(testCard.getCurrency());
        blockedCard.setBalance(testCard.getBalance());
        blockedCard.setActive(testCard.getActive());
        blockedCard.setIsBlocked(true);
        blockedCard.setUser(testCard.getUser());
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(blockedCard);
        when(dataEncryptor.decrypt(anyString())).thenReturn(CARD_NUMBER);
        when(dataMasker.maskCardNumber(anyString())).thenReturn(CARD_NUMBER_MASKED);
        CardDTO result = cardService.blockCard(1L);
        assertTrue(result.getIsBlocked());
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("Блокировка уже заблокированной карты выбрасывает исключение")
    void blockAlreadyBlockedCardTest() {
        Card alreadyBlocked = new Card();
        alreadyBlocked.setId(1L);
        alreadyBlocked.setIsBlocked(true);
        alreadyBlocked.setActive(true);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(alreadyBlocked));
        CardOperationException exception = assertThrows(CardOperationException.class,
                () -> cardService.blockCard(1L));
        assertEquals(CARD_ALREADY_BLOCKED_MESSAGE, exception.getMessage());
    }

    @Test
    @DisplayName("Разблокировка активной и заблокированной карты проходит успешно")
    void unblockCardWithValidCardTest() {
        Card blockedCard = new Card();

        blockedCard.setId(testCard.getId());
        blockedCard.setCardNumber(testCard.getCardNumber());
        blockedCard.setCardHolderName(testCard.getCardHolderName());
        blockedCard.setExpiryDate(testCard.getExpiryDate());
        blockedCard.setCardType(testCard.getCardType());
        blockedCard.setCurrency(testCard.getCurrency());
        blockedCard.setBalance(testCard.getBalance());
        blockedCard.setActive(testCard.getActive());
        blockedCard.setIsBlocked(true);
        blockedCard.setUser(testCard.getUser());

        Card unblockedCard = new Card();
        unblockedCard.setId(testCard.getId());
        unblockedCard.setCardNumber(testCard.getCardNumber());
        unblockedCard.setCardHolderName(testCard.getCardHolderName());
        unblockedCard.setExpiryDate(testCard.getExpiryDate());
        unblockedCard.setCardType(testCard.getCardType());
        unblockedCard.setCurrency(testCard.getCurrency());
        unblockedCard.setBalance(testCard.getBalance());
        unblockedCard.setActive(testCard.getActive());
        unblockedCard.setIsBlocked(false);
        unblockedCard.setUser(testCard.getUser());
        when(cardRepository.findById(1L)).thenReturn(Optional.of(blockedCard));
        when(cardRepository.save(any(Card.class))).thenReturn(unblockedCard);
        when(dataEncryptor.decrypt(anyString())).thenReturn(CARD_NUMBER);
        when(dataMasker.maskCardNumber(anyString())).thenReturn(CARD_NUMBER_MASKED);
        CardDTO result = cardService.unblockCard(1L);
        assertFalse(result.getIsBlocked());
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("Деактивация карты с нулевым балансом проходит успешно")
    void deactivateCardWithZeroBalanceTest() {
        Card deactivatedCard = new Card();
        deactivatedCard.setId(testCard.getId());
        deactivatedCard.setCardNumber(testCard.getCardNumber());
        deactivatedCard.setCardHolderName(testCard.getCardHolderName());
        deactivatedCard.setExpiryDate(testCard.getExpiryDate());
        deactivatedCard.setCardType(testCard.getCardType());
        deactivatedCard.setCurrency(testCard.getCurrency());
        deactivatedCard.setBalance(testCard.getBalance());
        deactivatedCard.setActive(false);
        deactivatedCard.setIsBlocked(testCard.getIsBlocked());
        deactivatedCard.setUser(testCard.getUser());
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(deactivatedCard);
        when(dataEncryptor.decrypt(anyString())).thenReturn(CARD_NUMBER);
        when(dataMasker.maskCardNumber(anyString())).thenReturn(CARD_NUMBER_MASKED);
        CardDTO result = cardService.deactivateCard(1L);
        assertFalse(result.getActive());
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("Деактивация карты с положительным балансом выбрасывает исключение")
    void deactivateCardWithPositiveBalanceTest() {
        Card cardWithBalance = new Card();
        cardWithBalance.setId(1L);
        cardWithBalance.setBalance(new BigDecimal("100.00"));
        cardWithBalance.setActive(true);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(cardWithBalance));
        CardOperationException exception = assertThrows(CardOperationException.class,
                () -> cardService.deactivateCard(1L));
        assertEquals(CANNOT_DEACTIVATE_WITH_BALANCE_MESSAGE, exception.getMessage());
    }

    @Test
    @DisplayName("Деактивация уже неактивной карты выбрасывает исключение")
    void deactivateInactiveCardTest() {
        Card inactiveCard = new Card();
        inactiveCard.setActive(false);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(inactiveCard));
        CardOperationException ex = assertThrows(CardOperationException.class,
                () -> cardService.deactivateCard(1L));
        assertTrue(ex.getMessage().contains("уже деактивирована"));
    }

    @Test
    @DisplayName("Валидация карты (активна, не заблокирована, не просрочена) успешна")
    void validateCardWithValidCardTest() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        boolean result = cardService.validateCardBasic(1L);
        assertTrue(result);
    }

    @Test
    @DisplayName("Валидация просроченной карты возвращает false")
    void validateCardWithExpiredCardTest() {
        Card expiredCard = new Card();
        expiredCard.setId(1L);
        expiredCard.setExpiryDate(LocalDate.now().minusDays(1));
        expiredCard.setActive(true);
        expiredCard.setIsBlocked(false);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(expiredCard));
        boolean result = cardService.validateCardBasic(1L);
        assertFalse(result);
    }

    @Test
    @DisplayName("Поиск карт пользователя с пагинацией возвращает PageResponse")
    void searchUserCardsTest() {
        CardSearchRequest searchRequest = new CardSearchRequest();
        Pageable pageable = mock(Pageable.class);
        Page<Card> cardPage = new PageImpl<>(Arrays.asList(testCard));
        when(cardRepository.findUserCardsWithFilters(eq(1L), isNull(), isNull(), isNull(), isNull(), isNull(), eq(pageable)))
                .thenReturn(cardPage);
        when(dataEncryptor.decrypt(ENCRYPTED_CARD_NUMBER)).thenReturn(CARD_NUMBER);
        when(dataMasker.maskCardNumber(CARD_NUMBER)).thenReturn(CARD_NUMBER_MASKED);
        PageResponse<CardDTO> result = cardService.searchUserCards(1L, searchRequest, pageable);
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getId());
        verify(cardRepository).findUserCardsWithFilters(eq(1L), isNull(), isNull(), isNull(), isNull(), isNull(), eq(pageable));
    }

    @Test
    @DisplayName("Получение всех карт пользователя возвращает список CardDTO")
    void getUserCardsTest() {
        when(cardRepository.findByUserId(1L)).thenReturn(Arrays.asList(testCard));
        when(dataEncryptor.decrypt(ENCRYPTED_CARD_NUMBER)).thenReturn(CARD_NUMBER);
        when(dataMasker.maskCardNumber(CARD_NUMBER)).thenReturn(CARD_NUMBER_MASKED);
        List<CardDTO> result = cardService.getUserCards(1L);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(cardRepository).findByUserId(1L);
    }

    @Test
    @DisplayName("Получение общего баланса по пользователю возвращает сумму")
    void getTotalBalanceTest() {
        when(cardRepository.getTotalBalanceByUserId(1L)).thenReturn(Optional.of(TOTAL_CARD_BALANCE));
        BigDecimal result = cardService.getTotalBalance(1L);
        assertEquals(TOTAL_CARD_BALANCE, result);
    }

    @Test
    @DisplayName("Если баланс не найден, getTotalBalance возвращает ZERO")
    void getTotalBalanceWhenNoCardsTest() {
        when(cardRepository.getTotalBalanceByUserId(1L)).thenReturn(Optional.empty());
        BigDecimal result = cardService.getTotalBalance(1L);
        assertEquals(BigDecimal.ZERO, result);
    }
}