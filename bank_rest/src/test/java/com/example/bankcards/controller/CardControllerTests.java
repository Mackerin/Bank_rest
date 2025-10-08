package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.CardType;
import com.example.bankcards.entity.Currency;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.CardSecurity;
import com.example.bankcards.security.UserPrincipal;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static com.example.bankcards.testConstants.JsonTestConstants.*;
import static com.example.bankcards.testConstants.UserTestConstants.*;
import static globalConstants.EndpointConstants.*;
import static globalConstants.MessageConstants.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Controller card tests")
@ActiveProfiles("test")
class CardControllerTests {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CardService cardService;
    @MockBean
    private CardSecurity cardSecurity;
    @Autowired
    private ObjectMapper objectMapper;
    private CardDTO cardDTO;
    private CreateCardRequest createCardRequest;
    private CardSearchRequest searchRequest;
    private UserPrincipal testUserPrincipal;

    @BeforeEach
    void setUp() {
        User mockUserEntity = User.builder()
                .id(1L)
                .username(TEST_USERNAME)
                .role(Role.ROLE_USER)
                .password(TEST_PASSWORD)
                .active(true)
                .build();

        testUserPrincipal = new UserPrincipal(mockUserEntity);
        cardDTO = new CardDTO(
                1L,
                CARD_NUMBER_MASKED,
                TEST_FIRST_NAME + " " + TEST_LAST_NAME,
                LocalDate.now().plusYears(2),
                CardType.DEBIT,
                Currency.USD,
                CARD_BALANCE,
                null,
                null,
                true,
                false,
                LocalDateTime.now(),
                1L,
                TEST_FIRST_NAME + " " + TEST_LAST_NAME
        );
        createCardRequest = new CreateCardRequest(CardType.DEBIT, Currency.USD, 1L);
        searchRequest = new CardSearchRequest();
        searchRequest.setPage(0);
        searchRequest.setSize(10);
        searchRequest.setSort(SORT_CREATED_AT_DESC);
    }

    @Test
    @WithMockUser(roles = ROLE_ADMIN)
    @DisplayName("Администратор успешно создаёт карту с валидными данными")
    void createCardAsAdminWithValidRequestTest() throws Exception {
        when(cardService.createCard(any(CreateCardRequest.class))).thenReturn(cardDTO);
        mockMvc.perform(post(CARDS_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCardRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(true))
                .andExpect(jsonPath(JSON_PATH_MESSAGE).value(CARD_CREATED_SUCCESS))
                .andExpect(jsonPath(JSON_PATH_DATA_ID).value(1L));
        verify(cardService).createCard(any(CreateCardRequest.class));
    }

    @Test
    @WithMockUser(roles = ROLE_USER)
    @DisplayName("Пользователь не может создать карту — запрещено для роли USER")
    void createCardAsUserTest() throws Exception {
        mockMvc.perform(post(CARDS_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCardRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Авторизованный пользователь успешно получает список своих карт")
    void getUserCardsWithAuthenticatedUserTest() throws Exception {
        List<CardDTO> cards = Arrays.asList(cardDTO);
        when(cardService.getUserCards(1L)).thenReturn(cards);
        mockMvc.perform(get(CARDS_BASE_PATH)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        testUserPrincipal,
                                        null,
                                        testUserPrincipal.getAuthorities()
                                )
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(true))
                .andExpect(jsonPath(JSON_PATH_DATA_ARRAY_ID).value(1L));
        verify(cardService).getUserCards(1L);
    }

    @Test
    @WithMockUser(roles = ROLE_ADMIN)
    @DisplayName("Администратор успешно получает статус карты")
    void getCardStatusAsAdminTest() throws Exception {
        CardDTO card = cardDTO.toBuilder().expiryDate(LocalDate.now().plusDays(10)).build();
        when(cardService.getCardById(1L)).thenReturn(card);
        mockMvc.perform(get(CARD_STATUS_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(true))
                .andExpect(jsonPath(JSON_PATH_DATA_STATUS).exists());
    }

    @Test
    @DisplayName("Пользователь успешно выполняет постраничный поиск своих карт")
    void searchCardsWithValidRequestTest() throws Exception {
        Page<CardDTO> cardPage = new PageImpl<>(Arrays.asList(cardDTO),
                PageRequest.of(0, 10), 1);
        PageResponse<CardDTO> pageResponse = new PageResponse<>(
                cardPage.getContent(),
                cardPage.getNumber(),
                cardPage.getSize(),
                cardPage.getTotalElements()
        );
        when(cardService.searchUserCards(eq(1L), any(CardSearchRequest.class), any(Pageable.class)))
                .thenReturn(pageResponse);
        mockMvc.perform(post(SEARCH_CARDS_ENDPOINT)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        testUserPrincipal,
                                        null,
                                        testUserPrincipal.getAuthorities()
                                )
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(true))
                .andExpect(jsonPath(JSON_PATH_DATA_CONTENT_ID).value(1L));
        verify(cardService).searchUserCards(eq(1L), any(CardSearchRequest.class), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = ROLE_ADMIN)
    @DisplayName("Администратор успешно получает данные карты по ID")
    void getCardByIdAsAdminTest() throws Exception {
        when(cardService.getCardById(1L)).thenReturn(cardDTO);
        mockMvc.perform(get(CARD_BY_ID_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(true))
                .andExpect(jsonPath(JSON_PATH_DATA_ID).value(1L));
        verify(cardService).getCardById(1L);
    }

    @Test
    @DisplayName("Владелец карты успешно получает её данные по ID")
    void getCardByIdAsOwnerTest() throws Exception {
        when(cardSecurity.isCardOwner(1L, 1L)).thenReturn(true);
        when(cardService.getCardById(1L)).thenReturn(cardDTO);
        mockMvc.perform(get(CARD_BY_ID_ENDPOINT)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        testUserPrincipal,
                                        null,
                                        testUserPrincipal.getAuthorities()
                                )
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(true))
                .andExpect(jsonPath(JSON_PATH_DATA_ID).value(1L));
        verify(cardService).getCardById(1L);
    }

    @Test
    @WithMockUser(roles = ROLE_ADMIN)
    @DisplayName("Администратор успешно получает список карт пользователя по ID")
    void getUserCardsByUserIdAsAdminTest() throws Exception {
        List<CardDTO> cards = Arrays.asList(cardDTO);
        when(cardService.getUserCards(2L)).thenReturn(cards);
        mockMvc.perform(get(USER_CARDS_BY_ID_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(true))
                .andExpect(jsonPath(JSON_PATH_DATA_ARRAY_ID).value(1L));
        verify(cardService).getUserCards(2L);
    }

    @Test
    @DisplayName("Владелец успешно блокирует свою карту")
    void blockCardAsOwnerTest() throws Exception {
        CardDTO blockedCard = cardDTO.toBuilder().isBlocked(true).build();
        when(cardService.blockCard(1L)).thenReturn(blockedCard);
        when(cardSecurity.isCardOwner(1L, 1L)).thenReturn(true);
        mockMvc.perform(patch(BLOCK_CARD_ENDPOINT)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        testUserPrincipal,
                                        null,
                                        testUserPrincipal.getAuthorities()
                                )
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(true))
                .andExpect(jsonPath(JSON_PATH_MESSAGE).value(CARD_BLOCKED_SUCCESS));
        verify(cardService).blockCard(1L);
    }

    @Test
    @DisplayName("Владелец успешно разблокирует свою карту")
    void unblockCardAsOwnerTest() throws Exception {
        CardDTO unblockedCard = cardDTO.toBuilder().isBlocked(false).build();
        when(cardService.unblockCard(1L)).thenReturn(unblockedCard);
        when(cardSecurity.isCardOwner(1L, 1L)).thenReturn(true);
        mockMvc.perform(patch(UNBLOCK_CARD_ENDPOINT)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        testUserPrincipal,
                                        null,
                                        testUserPrincipal.getAuthorities()
                                )
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(true))
                .andExpect(jsonPath(JSON_PATH_MESSAGE).value(CARD_UNBLOCKED_SUCCESS));
        verify(cardService).unblockCard(1L);
    }

    @Test
    @DisplayName("Владелец успешно запрашивает баланс своей карты")
    void getCardBalanceAsOwnerTest() throws Exception {
        when(cardService.getCardById(1L)).thenReturn(cardDTO);
        when(cardSecurity.isCardOwner(1L, 1L)).thenReturn(true);
        mockMvc.perform(get(CARD_BALANCE_ENDPOINT)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        testUserPrincipal,
                                        null,
                                        testUserPrincipal.getAuthorities()
                                )
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(true))
                .andExpect(jsonPath(JSON_PATH_DATA_BALANCE).value(1000.00))
                .andExpect(jsonPath(JSON_PATH_DATA_CURRENCY).value("USD"));
        verify(cardService).getCardById(1L);
    }

    @Test
    @DisplayName("Пользователь успешно получает общий баланс по всем своим картам")
    void getTotalBalanceTest() throws Exception {
        when(cardService.getTotalBalance(1L)).thenReturn(new BigDecimal("2500.00"));
        mockMvc.perform(get(TOTAL_BALANCE_ENDPOINT)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        testUserPrincipal,
                                        null,
                                        testUserPrincipal.getAuthorities()
                                )
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(true))
                .andExpect(jsonPath(JSON_PATH_DATA).value(2500.00));
        verify(cardService).getTotalBalance(1L);
    }

    @Test
    @DisplayName("Владелец успешно деактивирует свою карту")
    void deactivateCardAsOwnerTest() throws Exception {
        CardDTO deactivatedCard = cardDTO.toBuilder().active(false).build();
        when(cardService.deactivateCard(1L)).thenReturn(deactivatedCard);
        when(cardSecurity.isCardOwner(1L, 1L)).thenReturn(true);
        mockMvc.perform(delete(CARD_BY_ID_ENDPOINT)
                        .with(authentication(createAuthToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_MESSAGE).value(CARD_DEACTIVATED_SUCCESS));
    }

    @Test
    @DisplayName("Владелец успешно проверяет валидность своей карты")
    void validateCardWithValidCardTest() throws Exception {
        when(cardService.validateCardBasic(1L)).thenReturn(true);
        when(cardSecurity.isCardOwner(1L, 1L)).thenReturn(true);

        mockMvc.perform(post(VALIDATE_CARD_ENDPOINT)
                        .with(authentication(createAuthToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_MESSAGE).value(CARD_VALID_SUCCESS));
    }

    private UsernamePasswordAuthenticationToken createAuthToken() {
        return new UsernamePasswordAuthenticationToken(
                testUserPrincipal, null, testUserPrincipal.getAuthorities()
        );
    }
}