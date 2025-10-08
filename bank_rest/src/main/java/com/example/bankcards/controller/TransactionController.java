package com.example.bankcards.controller;

import com.example.bankcards.dto.ApiResponse;
import com.example.bankcards.dto.OwnCardsTransferRequest;
import com.example.bankcards.dto.TransactionDTO;
import com.example.bankcards.dto.TransactionRequest;
import com.example.bankcards.exception.ValidationException;
import com.example.bankcards.security.CardSecurity;
import com.example.bankcards.security.UserPrincipal;
import com.example.bankcards.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

import static globalConstants.AuthorizationConstants.*;
import static globalConstants.EndpointConstants.*;
import static globalConstants.MessageConstants.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Транзакции", description = "API для управления транзакциями")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;
    private final CardSecurity cardSecurity;

    @PostMapping("/transfer")
    @Operation(summary = "Перевод денег", description = "Выполнение перевода денег между картами")
    public ResponseEntity<ApiResponse> transferMoney(@Valid @RequestBody TransactionRequest transactionRequest,
                                                     @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (!cardSecurity.isCardOwner(transactionRequest.getFromCardId(), userPrincipal.getId())) {
            throw new AccessDeniedException("Доступ запрещен");
        }
        TransactionDTO transactionDTO = transactionService.transferMoney(transactionRequest);
        return ResponseEntity.ok(ApiResponse.success(TRANSFER_SUCCESS_MESSAGE,
                transactionDTO, TRANSFER_ENDPOINT));
    }

    @PostMapping("/transfer-between-own-cards")
    @Operation(summary = "Перевод между своими картами",
            description = "Выполнение перевода между картами текущего пользователя")
    @PreAuthorize(OWNER_OF_BOTH_CARDS)
    public ResponseEntity<ApiResponse> transferBetweenOwnCards(
            @Valid @RequestBody OwnCardsTransferRequest transferRequest,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getId();
        TransactionDTO transactionDTO = transactionService.transferBetweenOwnCards(transferRequest, userId);
        return ResponseEntity.ok(ApiResponse.success(TRANSFER_BETWEEN_OWN_CARDS_SUCCESS_MESSAGE,
                transactionDTO, TRANSFER_BETWEEN_OWN_CARDS_ENDPOINT));
    }

    @PostMapping("/deposit")
    @Operation(summary = "Пополнение счета", description = "Пополнение баланса карты (доступно только ADMIN)")
    @PreAuthorize(HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse> depositMoney(
            @RequestParam Long cardId,
            @RequestParam BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(DEPOSIT_AMOUNT_MUST_BE_POSITIVE_MESSAGE);
        }
        TransactionDTO transactionDTO = transactionService.depositMoney(cardId, amount);
        return ResponseEntity.ok(ApiResponse.success(DEPOSIT_SUCCESS_MESSAGE,
                transactionDTO, DEPOSIT_ENDPOINT));
    }

    @GetMapping
    @Operation(summary = "История транзакций", description = "Получение истории транзакций текущего пользователя")
    public ResponseEntity<ApiResponse> getUserTransactions(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long currentUserId = userPrincipal.getId();
        List<TransactionDTO> transactions = transactionService.getUserTransactions(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(GET_TRANSACTIONS_SUCCESS_MESSAGE,
                transactions, GET_TRANSACTIONS_ENDPOINT));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получение транзакции по ID", description = "Получение информации о транзакции по ID")
    @PreAuthorize(TRANSACTION_PARTICIPANT_OR_ADMIN_BY_ID)
    public ResponseEntity<ApiResponse> getTransactionById(@PathVariable Long id) {
        TransactionDTO transactionDTO = transactionService.getTransactionById(id);
        return ResponseEntity.ok(ApiResponse.success(GET_TRANSACTION_SUCCESS_MESSAGE,
                transactionDTO, GET_TRANSACTIONS_ENDPOINT + "/" + id));
    }

    @GetMapping("/transaction-id/{transactionId}")
    @Operation(summary = "Получение транзакции по номеру",
            description = "Получение информации о транзакции по её уникальному номеру")
    @PreAuthorize(TRANSACTION_PARTICIPANT_OR_ADMIN_BY_TRANSACTION_ID)
    public ResponseEntity<ApiResponse> getTransactionByTransactionId(@PathVariable String transactionId) {
        TransactionDTO transactionDTO = transactionService.getTransactionByTransactionId(transactionId);
        return ResponseEntity.ok(ApiResponse.success(GET_TRANSACTION_SUCCESS_MESSAGE,
                transactionDTO, GET_TRANSACTIONS_ENDPOINT + "/transaction-id/" + transactionId));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Транзакции пользователя",
            description = "Получение транзакций указанного пользователя (доступно только ADMIN)")
    @PreAuthorize(HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse> getUserTransactionsByUserId(@PathVariable Long userId) {
        List<TransactionDTO> transactions = transactionService.getUserTransactions(userId);
        return ResponseEntity.ok(ApiResponse.success(GET_USER_TRANSACTIONS_SUCCESS_MESSAGE,
                transactions, GET_TRANSACTIONS_ENDPOINT + "/user/" + userId));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Отмена транзакции", description = "Отмена pending транзакции")
    @PreAuthorize(TRANSACTION_INITIATOR_OR_ADMIN)
    public ResponseEntity<ApiResponse> cancelTransaction(@PathVariable Long id) {
        transactionService.cancelTransaction(id);
        return ResponseEntity.ok(ApiResponse.success(CANCEL_TRANSACTION_SUCCESS_MESSAGE, null,
                GET_TRANSACTIONS_ENDPOINT + "/" + id + "/cancel"));
    }
}