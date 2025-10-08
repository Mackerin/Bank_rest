package com.example.bankcards.service;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardType;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.CardOperationException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.DataEncryptor;
import com.example.bankcards.util.DataMasker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static globalConstants.MessageConstants.CANNOT_DEACTIVATE_WITH_BALANCE_MESSAGE;
import static globalConstants.MessageConstants.CARD_ALREADY_BLOCKED_MESSAGE;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final DataMasker dataMasker;
    private final DataEncryptor dataEncryptor;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.bank.credit-card-limit:50000.00}")
    private BigDecimal creditCardLimit;

    @Value("${app.bank.default-currency:RUB}")
    private String defaultCurrency;

    @Value("${app.card.expiry-years:4}")
    private int cardExpiryYears;

    public CardDTO createCard(CreateCardRequest request) {
        log.info("Создание карты для пользователя: {}, тип: {}", request.getUserId(), request.getCardType());
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId().toString()));
        String cardNumber = generateCardNumber();
        LocalDate expiryDate = LocalDate.now().plusYears(cardExpiryYears);
        Card card = new Card();
        card.setCardNumber(dataEncryptor.encrypt(cardNumber));
        card.setCardHolderName(user.getFirstName() + " " + user.getLastName());
        card.setExpiryDate(expiryDate);
        card.setCardType(request.getCardType());
        card.setCurrency(request.getCurrency() != null ? request.getCurrency() : com.example.bankcards.entity.
                Currency.valueOf(defaultCurrency));
        card.setBalance(BigDecimal.ZERO);
        card.setActive(true);
        card.setIsBlocked(false);
        card.setUser(user);
        if (request.getCardType() == CardType.CREDIT) {
            card.setCreditLimit(creditCardLimit);
        }
        Card savedCard = cardRepository.save(card);
        log.info("Карта успешно создана с ID: {} для пользователя: {}", savedCard.getId(), request.getUserId());
        return convertToDTO(savedCard);
    }

    @Transactional(readOnly = true)
    public PageResponse<CardDTO> searchUserCards(Long userId, CardSearchRequest searchRequest, Pageable pageable) {
        log.info("Поиск карт пользователя: {} с фильтрами: {}", userId, searchRequest);
        Page<Card> cardPage = cardRepository.findUserCardsWithFilters(
                userId,
                null,
                searchRequest.getCardType(),
                searchRequest.getCurrency(),
                searchRequest.getActive(),
                searchRequest.getIsBlocked(),
                pageable
        );
        List<CardDTO> cardDTOs = cardPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        log.info("Найдено {} карт пользователя: {}", cardDTOs.size(), userId);
        return new PageResponse<>(
                cardDTOs,
                cardPage.getNumber(),
                cardPage.getSize(),
                cardPage.getTotalElements()
        );
    }

    public boolean validateCardBasic(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        boolean cardValid = card.getActive() &&
                !card.getIsBlocked() &&
                card.getExpiryDate().isAfter(LocalDate.now());
        if (!cardValid) {
            return false;
        }
        return true;
    }

    @Transactional(readOnly = true)
    public CardDTO getCardById(Long id) {
        log.debug("Получение карты по ID: {}", id);
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
        return convertToDTO(card);
    }

    @Transactional(readOnly = true)
    public List<CardDTO> getUserCards(Long userId) {
        log.debug("Получение всех карт пользователя: {}", userId);
        return cardRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CardDTO> getActiveUserCards(Long userId) {
        log.debug("Получение активных карт пользователя: {}", userId);
        return cardRepository.findByUserIdAndActiveTrue(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CardDTO blockCard(Long cardId) {
        log.info("Блокировка карты: {}", cardId);
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        if (!card.getActive()) {
            throw new CardOperationException("Нельзя заблокировать неактивную карту!");
        }
        if (card.getIsBlocked()) {
            throw new CardOperationException(CARD_ALREADY_BLOCKED_MESSAGE);
        }
        card.setIsBlocked(true);
        Card updatedCard = cardRepository.save(card);
        log.info("Карта успешно заблокирована: {}", cardId);
        return convertToDTO(updatedCard);
    }

    public CardDTO unblockCard(Long cardId) {
        log.info("Разблокирование карты: {}", cardId);
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        if (!card.getActive()) {
            throw new CardOperationException("Нельзя разблокировать неактивную карту!");
        }
        if (!card.getIsBlocked()) {
            throw new CardOperationException("Карта не заблокирована");
        }
        card.setIsBlocked(false);
        Card updatedCard = cardRepository.save(card);
        log.info("Карта разблокирована успешно: {}", cardId);
        return convertToDTO(updatedCard);
    }

    public CardDTO deactivateCard(Long cardId) {
        log.info("Деактивация карты: {}", cardId);
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        if (!card.getActive()) {
            throw new CardOperationException("Карта уже деактивирована!");
        }
        if (card.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new CardOperationException(CANNOT_DEACTIVATE_WITH_BALANCE_MESSAGE);
        }
        card.setActive(false);
        Card updatedCard = cardRepository.save(card);
        log.info("Карта успешно деактивирована: {}", cardId);
        return convertToDTO(updatedCard);
    }

    public BigDecimal getTotalBalance(Long userId) {
        log.debug("Вычисление общего баланса пользователя: {}", userId);
        return cardRepository.getTotalBalanceByUserId(userId)
                .orElse(BigDecimal.ZERO);
    }

    private String generateCardNumber() {
        StringBuilder sb = new StringBuilder();
        sb.append(secureRandom.nextInt(2) == 0 ? 4 : 5);
        for (int i = 0; i < 15; i++) {
            sb.append(secureRandom.nextInt(10));
        }
        return sb.toString();
    }

    private CardDTO convertToDTO(Card card) {
        CardDTO cardDTO = new CardDTO();
        cardDTO.setId(card.getId());
        try {
            String decryptedCardNumber = dataEncryptor.decrypt(card.getCardNumber());
            cardDTO.setCardNumber(dataMasker.maskCardNumber(decryptedCardNumber));
        } catch (Exception e) {
            log.error("Ошибка расшифровки номера карты для DTO", e);
            cardDTO.setCardNumber("**** **** **** ****");
        }
        cardDTO.setCardHolderName(card.getCardHolderName());
        cardDTO.setExpiryDate(card.getExpiryDate());
        cardDTO.setCardType(card.getCardType());
        cardDTO.setCurrency(card.getCurrency());
        cardDTO.setBalance(card.getBalance());
        cardDTO.setCreditLimit(card.getCreditLimit());
        cardDTO.setActive(card.getActive());
        cardDTO.setIsBlocked(card.getIsBlocked());
        cardDTO.setCreatedAt(card.getCreatedAt());
        cardDTO.setUserId(card.getUser().getId());
        cardDTO.setUserFullName(card.getUser().getFirstName() + " " + card.getUser().getLastName());
        return cardDTO;
    }
}