package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.TransactionStatus;
import com.example.bankcards.entity.TransactionType;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.CardSecurity;
import com.example.bankcards.security.TransactionSecurity;
import com.example.bankcards.security.UserPrincipal;
import com.example.bankcards.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static com.example.bankcards.testConstants.MessageTestConstants.*;
import static com.example.bankcards.testConstants.TransactionTestConstants.*;
import static com.example.bankcards.testConstants.UserTestConstants.*;
import static com.example.bankcards.testConstants.JsonTestConstants.*;
import static globalConstants.EndpointConstants.*;
import static globalConstants.MessageConstants.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureMockMvc
@DisplayName("Transaction controller tests")
@ActiveProfiles("test")
class TransactionControllerTests {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private TransactionService transactionService;
    @MockBean
    private CardSecurity cardSecurity;
    @MockBean
    private TransactionSecurity transactionSecurity;
    @Autowired
    private ObjectMapper objectMapper;
    private TransactionDTO transactionDTO;
    private TransactionRequest transactionRequest;
    private OwnCardsTransferRequest ownCardsTransferRequest;
    private UserPrincipal testUserPrincipal;

    @BeforeEach
    void setUp() {
        User mockUserEntity = new User();
        mockUserEntity.setId(1L);
        mockUserEntity.setUsername(TEST_USERNAME);
        mockUserEntity.setRole(Role.ROLE_USER);
        mockUserEntity.setPassword(TEST_PASSWORD);
        mockUserEntity.setActive(true);

        testUserPrincipal = new UserPrincipal(mockUserEntity);

        transactionDTO = new TransactionDTO();
        transactionDTO.setId(1L);
        transactionDTO.setTransactionId(TRANSACTION_ID);
        transactionDTO.setFromCardId(1L);
        transactionDTO.setToCardId(2L);
        transactionDTO.setAmount(CARD_BALANCE);
        transactionDTO.setType(TransactionType.TRANSFER);
        transactionDTO.setStatus(TransactionStatus.COMPLETED);
        transactionDTO.setDescription(TEST_TRANSACTION_DESCRIPTION);
        transactionDTO.setCommission(BigDecimal.ZERO);
        transactionDTO.setCreatedAt(LocalDateTime.now());
        transactionDTO.setFromUserId(1L);
        transactionDTO.setFromUserFullName(TEST_FROM_USER_FULL_NAME);
        transactionDTO.setFromCardNumber(TEST_FROM_CARD_NUMBER);
        transactionDTO.setToUserId(2L);
        transactionDTO.setToUserFullName(TEST_TO_USER_FULL_NAME);
        transactionDTO.setToCardNumber(TEST_TO_CARD_NUMBER);

        transactionRequest = new TransactionRequest();
        transactionRequest.setFromCardId(1L);
        transactionRequest.setToCardNumber(TEST_TO_CARD_NUMBER);
        transactionRequest.setAmount(CARD_BALANCE);
        transactionRequest.setDescription(TEST_TRANSFER_DESCRIPTION);

        ownCardsTransferRequest = new OwnCardsTransferRequest();
        ownCardsTransferRequest.setFromCardId(1L);
        ownCardsTransferRequest.setToCardId(2L);
        ownCardsTransferRequest.setAmount(new BigDecimal("50.00"));
        ownCardsTransferRequest.setDescription(TEST_OWN_CARDS_TRANSFER_DESCRIPTION);
    }

    @Test
    @DisplayName("Успешный перевод на карту другого пользователя")
    void transferMoneyWithValidRequestTest() throws Exception {
        when(cardSecurity.isCardOwner(eq(1L), eq(1L))).thenReturn(true);
        when(transactionService.transferMoney(any(TransactionRequest.class))).thenReturn(transactionDTO);
        mockMvc.perform(post(TRANSFER_ENDPOINT)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        testUserPrincipal,
                                        null,
                                        testUserPrincipal.getAuthorities()
                                )
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(true))
                .andExpect(jsonPath(JSON_PATH_MESSAGE).value(TRANSFER_SUCCESS_MESSAGE))
                .andExpect(jsonPath(JSON_PATH_DATA_ID).value(1L));
        verify(transactionService).transferMoney(any(TransactionRequest.class));
        verify(cardSecurity).isCardOwner(eq(1L), eq(1L));
    }

    @Test
    @DisplayName("Ошибка при переводе с некорректным номером карты получателя")
    void transferMoneyWithInvalidToCardNumberTest() throws Exception {
        when(cardSecurity.isCardOwner(1L, 1L)).thenReturn(true);
        TransactionRequest invalidRequest = new TransactionRequest();
        invalidRequest.setFromCardId(1L);
        invalidRequest.setToCardNumber("123");
        invalidRequest.setAmount(new BigDecimal("100"));
        mockMvc.perform(post(TRANSFER_ENDPOINT)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        testUserPrincipal,
                                        null,
                                        testUserPrincipal.getAuthorities()
                                )
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        verify(transactionService, never()).transferMoney(any(TransactionRequest.class));
    }

    @Test
    @DisplayName("Ошибка при переводе с amount = null")
    void transferMoneyWithNullAmountTest() throws Exception {
        when(cardSecurity.isCardOwner(1L, 1L)).thenReturn(true);
        TransactionRequest invalidRequest = new TransactionRequest();
        invalidRequest.setFromCardId(1L);
        invalidRequest.setToCardNumber(TEST_TO_CARD_NUMBER);
        mockMvc.perform(post(TRANSFER_ENDPOINT)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        testUserPrincipal,
                                        null,
                                        testUserPrincipal.getAuthorities()
                                )
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        verify(transactionService, never()).transferMoney(any(TransactionRequest.class));
    }

    @Test
    @DisplayName("Запрет перевода с чужой карты")
    void transferMoneyWithNoAccessTest() throws Exception {
        when(cardSecurity.isCardOwner(eq(1L), anyLong())).thenReturn(false);
        mockMvc.perform(post(TRANSFER_ENDPOINT)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        testUserPrincipal,
                                        null,
                                        testUserPrincipal.getAuthorities()
                                )
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isForbidden());
        verify(transactionService, never()).transferMoney(any(TransactionRequest.class));
        verify(cardSecurity).isCardOwner(eq(1L), anyLong());
    }

    @Test
    @DisplayName("Успешный перевод между собственными картами")
    void transferBetweenOwnCardsWithValidRequestTest() throws Exception {
        when(cardSecurity.isCardOwner(1L, 1L)).thenReturn(true);
        when(cardSecurity.isCardOwner(2L, 1L)).thenReturn(true);
        when(transactionService.transferBetweenOwnCards(any(OwnCardsTransferRequest.class), eq(1L)))
                .thenReturn(transactionDTO);
        mockMvc.perform(post(TRANSFER_BETWEEN_OWN_CARDS_ENDPOINT)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        testUserPrincipal,
                                        null,
                                        testUserPrincipal.getAuthorities()
                                )
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ownCardsTransferRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(true))
                .andExpect(jsonPath(JSON_PATH_MESSAGE).value(TRANSFER_BETWEEN_OWN_CARDS_SUCCESS_MESSAGE));
        verify(transactionService).transferBetweenOwnCards(any(OwnCardsTransferRequest.class), eq(1L));
    }

    @Test
    @DisplayName("Запрет перевода между картами, если одна из них не принадлежит пользователю")
    void transferBetweenOwnCardsWithNoAccessToToCardTest() throws Exception {
        when(cardSecurity.isCardOwner(1L, 1L)).thenReturn(true);
        when(cardSecurity.isCardOwner(2L, 1L)).thenReturn(false);
        mockMvc.perform(post(TRANSFER_BETWEEN_OWN_CARDS_ENDPOINT)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        testUserPrincipal,
                                        null,
                                        testUserPrincipal.getAuthorities()
                                )
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ownCardsTransferRequest)))
                .andExpect(status().isForbidden());
        verify(transactionService, never()).transferBetweenOwnCards(any(), anyLong());
    }

    @Test
    @WithMockUser(username = TEST_ADMIN, roles = ROLE_ADMIN)
    @DisplayName("Администратор успешно пополняет карту (корректная сумма)")
    void depositMoneyWithValidAmountTest() throws Exception {
        when(transactionService.depositMoney(1L, new BigDecimal("500.00"))).thenReturn(transactionDTO);
        mockMvc.perform(post(DEPOSIT_ENDPOINT)
                        .param(PARAM_CARD_ID, "1")
                        .param(PARAM_AMOUNT, "500.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(true))
                .andExpect(jsonPath(JSON_PATH_MESSAGE).value(DEPOSIT_SUCCESS_MESSAGE));
        verify(transactionService).depositMoney(1L, new BigDecimal("500.00"));
    }

    @Test
    @WithMockUser(username = TEST_ADMIN, roles = ROLE_ADMIN)
    @DisplayName("Ошибка при попытке пополнить карту нулевой или отрицательной суммой")
    void depositMoneyWithInvalidAmountTest() throws Exception {
        mockMvc.perform(post(DEPOSIT_ENDPOINT)
                        .param(PARAM_CARD_ID, "1")
                        .param(PARAM_AMOUNT, "0"))
                .andExpect(status().isBadRequest());
        verify(transactionService, never()).depositMoney(anyLong(), any());
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, roles = ROLE_USER)
    @DisplayName("Обычный пользователь не может пополнять карты")
    void depositMoneyAsUserTest() throws Exception {
        mockMvc.perform(post(DEPOSIT_ENDPOINT)
                        .param(PARAM_CARD_ID, "1")
                        .param(PARAM_AMOUNT, "500.00"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Пользователь успешно получает список своих транзакций")
    void getUserTransactionsTest() throws Exception {
        List<TransactionDTO> transactions = Arrays.asList(transactionDTO);
        when(transactionService.getUserTransactions(1L)).thenReturn(transactions);
        mockMvc.perform(get(GET_TRANSACTIONS_ENDPOINT)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        testUserPrincipal,
                                        null,
                                        testUserPrincipal.getAuthorities()
                                )
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(true))
                .andExpect(jsonPath(JSON_PATH_MESSAGE).value(GET_TRANSACTIONS_SUCCESS_MESSAGE))
                .andExpect(jsonPath(JSON_PATH_DATA_ARRAY_ID).value(1L));
        verify(transactionService).getUserTransactions(1L);
    }

    @Test
    @DisplayName("Участник транзакции успешно получает её детали")
    void getTransactionByIdAsParticipantTest() throws Exception {
        when(transactionSecurity.isTransactionParticipant(1L, 1L)).thenReturn(true);
        when(transactionService.getTransactionById(1L)).thenReturn(transactionDTO);
        mockMvc.perform(get(GET_TRANSACTION_BY_ID_ENDPOINT)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        testUserPrincipal,
                                        null,
                                        testUserPrincipal.getAuthorities()
                                )
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(true))
                .andExpect(jsonPath(JSON_PATH_MESSAGE).value(GET_TRANSACTION_SUCCESS_MESSAGE))
                .andExpect(jsonPath(JSON_PATH_DATA_ID).value(1L));
        verify(transactionService).getTransactionById(1L);
        verify(transactionSecurity).isTransactionParticipant(1L, 1L);
    }

    @Test
    @DisplayName("Пользователь не может просматривать конкретную чужую транзакцию")
    void getTransactionByIdAsNonParticipantTest() throws Exception {
        when(transactionSecurity.isTransactionParticipant(1L, 1L)).thenReturn(false);
        mockMvc.perform(get(GET_TRANSACTION_BY_ID_ENDPOINT)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        testUserPrincipal,
                                        null,
                                        testUserPrincipal.getAuthorities()
                                )
                        )))
                .andExpect(status().isForbidden());
        verify(transactionService, never()).getTransactionById(anyLong());
        verify(transactionSecurity).isTransactionParticipant(1L, 1L);
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, roles = ROLE_USER)
    @DisplayName("Пользователь не может просматривать список чужих транзакций")
    void getUserTransactionsByUserIdAsUserTest() throws Exception {
        mockMvc.perform(get(GET_USER_TRANSACTIONS_ENDPOINT))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Участник успешно получает транзакцию по уникальному идентификатору")
    void getTransactionByTransactionIdAsParticipantTest() throws Exception {
        when(transactionSecurity.isTransactionParticipantByTransactionId(ANOTHER_TRANSACTION_ID, 1L)).
                thenReturn(true);
        when(transactionService.getTransactionByTransactionId(ANOTHER_TRANSACTION_ID)).thenReturn(transactionDTO);
        mockMvc.perform(get(GET_TRANSACTION_BY_TRANSACTION_ID_ENDPOINT)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        testUserPrincipal,
                                        null,
                                        testUserPrincipal.getAuthorities()
                                )
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(true))
                .andExpect(jsonPath(JSON_PATH_MESSAGE).value(GET_TRANSACTION_SUCCESS_MESSAGE))
                .andExpect(jsonPath(JSON_PATH_DATA_TRANSACTION_ID).value(TRANSACTION_ID));
        verify(transactionService).getTransactionByTransactionId(ANOTHER_TRANSACTION_ID);
        verify(transactionSecurity).isTransactionParticipantByTransactionId(ANOTHER_TRANSACTION_ID, 1L);
    }

    @Test
    @WithMockUser(username = TEST_ADMIN, roles = ROLE_ADMIN)
    @DisplayName("Администратор успешно просматривает транзакции любого пользователя")
    void getUserTransactionsByUserIdAsAdminTest() throws Exception {
        List<TransactionDTO> transactions = Arrays.asList(transactionDTO);
        when(transactionService.getUserTransactions(2L)).thenReturn(transactions);
        mockMvc.perform(get(GET_USER_TRANSACTIONS_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(true))
                .andExpect(jsonPath(JSON_PATH_MESSAGE).value(GET_USER_TRANSACTIONS_SUCCESS_MESSAGE))
                .andExpect(jsonPath(JSON_PATH_DATA_ARRAY_ID).value(1L));
        verify(transactionService).getUserTransactions(2L);
    }

    @Test
    @DisplayName("Инициатор транзакции успешно отменяет её")
    void cancelTransactionAsInitiatorTest() throws Exception {
        when(transactionSecurity.isTransactionInitiator(1L, 1L)).thenReturn(true);
        doNothing().when(transactionService).cancelTransaction(1L);
        mockMvc.perform(patch(CANCEL_TRANSACTION_ENDPOINT)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        testUserPrincipal,
                                        null,
                                        testUserPrincipal.getAuthorities()
                                )
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(true))
                .andExpect(jsonPath(JSON_PATH_MESSAGE).value(CANCEL_TRANSACTION_SUCCESS_MESSAGE));
        verify(transactionService).cancelTransaction(1L);
        verify(transactionSecurity).isTransactionInitiator(1L, 1L);
    }
}