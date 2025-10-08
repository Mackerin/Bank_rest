package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.security.UserPrincipal;
import com.example.bankcards.service.CardService;
import com.example.bankcards.security.CardSecurity;
import com.example.bankcards.exception.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static globalConstants.AuthorizationConstants.CARD_OWNER_OR_ADMIN;
import static globalConstants.AuthorizationConstants.HAS_ROLE_ADMIN;
import static globalConstants.CardStatusConstants.*;
import static globalConstants.EndpointConstants.*;
import static globalConstants.MessageConstants.*;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Tag(name = "Карты", description = "API для управления банковскими картами")
@SecurityRequirement(name = "bearerAuth")
public class CardController {

    private final CardService cardService;
    private final CardSecurity cardSecurity;

    @PostMapping
    @Operation(summary = "Создание карты", description = "Создание новой банковской карты для пользователя")
    @PreAuthorize(HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse> createCard(@Valid @RequestBody CreateCardRequest createCardRequest) {
        CardDTO cardDTO = cardService.createCard(createCardRequest);
        return ResponseEntity.ok(ApiResponse.success(CARD_CREATED_SUCCESS, cardDTO, CARDS_BASE_PATH));
    }

    @GetMapping
    @Operation(summary = "Получение всех карт пользователя",
            description = "Получение списка всех карт текущего пользователя")
    public ResponseEntity<ApiResponse> getUserCards(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long currentUserId = userPrincipal.getId();
        List<CardDTO> cards = cardService.getUserCards(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(GET_USERS_CARDS_SUCCESS_MESSAGE, cards,
                CARDS_BASE_PATH));
    }

    @PostMapping("/search")
    @Operation(summary = "Поиск карт с пагинацией", description = "Поиск карт с фильтрацией и постраничной выдачей")
    public ResponseEntity<ApiResponse> searchCards(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CardSearchRequest searchRequest) {
        Long userId = userPrincipal.getId();
        String[] sortParams = searchRequest.getSort().split(",");
        Sort sort = sortParams.length == 2
                ? Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0])
                : Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);
        PageResponse<CardDTO> pageResponse = cardService.searchUserCards(userId, searchRequest, pageable);
        return ResponseEntity.ok(ApiResponse.success(GET_USERS_CARDS_SUCCESS_MESSAGE, pageResponse,
                SEARCH_CARDS_ENDPOINT));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получение карты по ID", description = "Получение информации о карте по её ID")
    @PreAuthorize(CARD_OWNER_OR_ADMIN)
    public ResponseEntity<ApiResponse> getCardById(@PathVariable("id") Long id,
                                                   @AuthenticationPrincipal UserPrincipal userPrincipal) {
        CardDTO cardDTO = cardService.getCardById(id);
        return ResponseEntity.ok(ApiResponse.success(GET_CARD_BY_ID_SUCCESS_MESSAGE, cardDTO,
                CARDS_BASE_PATH + "/" + id));
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Получение статуса карты",
            description = "Получение детального статуса карты (Активна, Заблокирована, Истек срок)")
    @PreAuthorize(CARD_OWNER_OR_ADMIN)
    public ResponseEntity<ApiResponse> getCardStatus(@PathVariable Long id) {
        CardDTO cardDTO = cardService.getCardById(id);
        String status;
        String statusDescription;
        boolean expired = cardDTO.getExpiryDate().isBefore(LocalDate.now());
        boolean valid = cardDTO.getActive() != null && cardDTO.getActive() &&
                cardDTO.getIsBlocked() != null && !cardDTO.getIsBlocked() && !expired;
        long daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), cardDTO.getExpiryDate());
        if (cardDTO.getActive() == null || !cardDTO.getActive()) {
            status = DEACTIVATED_STATUS;
            statusDescription = DEACTIVATED_STATUS_DESCRIPTION;
        } else if (cardDTO.getIsBlocked() != null && cardDTO.getIsBlocked()) {
            status = BLOCKED_STATUS;
            statusDescription = BLOCKED_STATUS_DESCRIPTION;
        } else if (expired) {
            status = EXPIRED_STATUS;
            statusDescription = EXPIRED_STATUS_DESCRIPTION;
        } else {
            status = ACTIVE_STATUS;
            statusDescription = ACTIVE_STATUS_DESCRIPTION;
        }
        CardStatusResponse statusResponse = new CardStatusResponse(
                status, statusDescription, cardDTO.getActive(),
                cardDTO.getIsBlocked(), expired, valid,
                cardDTO.getExpiryDate(), daysUntilExpiry, cardDTO
        );
        return ResponseEntity.ok(ApiResponse.success(GET_CARD_STATUS_MESSAGE,
                statusResponse, CARDS_BASE_PATH + "/" + id + "/status"));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Получение карт пользователя",
            description = "Получение списка карт указанного пользователя (доступно только ADMIN)")
    @PreAuthorize(HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse> getUserCardsByUserId(@PathVariable("userId") Long userId) {
        List<CardDTO> cards = cardService.getUserCards(userId);
        return ResponseEntity.ok(ApiResponse.success(GET_USERS_CARDS_SUCCESS_MESSAGE,
                cards, CARDS_BASE_PATH + "/user/" + userId));
    }

    @PatchMapping("/{id}/block")
    @Operation(summary = "Блокировка карты", description = "Блокировка карты по ID")
    @PreAuthorize(CARD_OWNER_OR_ADMIN)
    public ResponseEntity<ApiResponse> blockCard(@PathVariable("id") Long id) {
        CardDTO cardDTO = cardService.blockCard(id);
        return ResponseEntity.ok(ApiResponse.success(CARD_BLOCKED_SUCCESS,
                cardDTO, CARDS_BASE_PATH + "/" + id + "/block"));
    }

    @PatchMapping("/{id}/unblock")
    @Operation(summary = "Разблокировка карты", description = "Разблокировка карты по ID")
    @PreAuthorize(CARD_OWNER_OR_ADMIN)
    public ResponseEntity<ApiResponse> unblockCard(@PathVariable("id") Long id) {
        CardDTO cardDTO = cardService.unblockCard(id);
        return ResponseEntity.ok(ApiResponse.success(CARD_UNBLOCKED_SUCCESS,
                cardDTO, CARDS_BASE_PATH + "/" + id + "/unblock"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Деактивация карты", description = "Деактивация карты по ID")
    @PreAuthorize(CARD_OWNER_OR_ADMIN)
    public ResponseEntity<ApiResponse> deactivateCard(@PathVariable Long id) {
        CardDTO cardDTO = cardService.deactivateCard(id);
        return ResponseEntity.ok(ApiResponse.success(CARD_DEACTIVATED_SUCCESS,
                cardDTO, CARDS_BASE_PATH + "/" + id));
    }

    @GetMapping("/{id}/balance")
    @Operation(summary = "Получение баланса карты", description = "Получение текущего баланса карты")
    @PreAuthorize(CARD_OWNER_OR_ADMIN)
    public ResponseEntity<ApiResponse> getCardBalance(@PathVariable Long id) {
        CardDTO cardDTO = cardService.getCardById(id);
        BalanceResponse balanceResponse = new BalanceResponse(
                cardDTO.getId(),
                cardDTO.getCardNumber(),
                cardDTO.getBalance(),
                cardDTO.getCurrency().name()
        );
        return ResponseEntity.ok(ApiResponse.success(GET_BALANCE_SUCCESS_MESSAGE,
                balanceResponse, CARDS_BASE_PATH + "/" + id + "/balance"));
    }

    @GetMapping("/total-balance")
    @Operation(summary = "Общий баланс пользователя", description = "Получение общего баланса всех карт пользователя")
    public ResponseEntity<ApiResponse> getTotalBalance(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getId();
        BigDecimal totalBalance = cardService.getTotalBalance(userId);
        return ResponseEntity.ok(ApiResponse.success(GET_TOTAL_BALANCE_SUCCESS_MESSAGE,
                totalBalance, TOTAL_BALANCE_ENDPOINT));
    }

    @PostMapping("/{id}/validate")
    @Operation(summary = "Проверка карты", description = "Проверка валидности карты (без CVV)")
    @PreAuthorize(CARD_OWNER_OR_ADMIN)
    public ResponseEntity<ApiResponse> validateCard(@PathVariable Long id) {
        boolean isValid = cardService.validateCardBasic(id);
        if (isValid) {
            return ResponseEntity.ok(ApiResponse.success(CARD_VALID_SUCCESS, null,
                    CARDS_BASE_PATH + "/" + id + "/validate"));
        } else {
            throw new ValidationException("Карта недействительна");
        }
    }

    public record BalanceResponse(Long cardId, String cardNumber, BigDecimal balance, String currency) {
    }
}