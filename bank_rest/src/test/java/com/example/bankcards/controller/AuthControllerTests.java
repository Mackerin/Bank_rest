package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Role;
import com.example.bankcards.exception.AuthenticationException;
import com.example.bankcards.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static com.example.bankcards.testConstants.JsonTestConstants.*;
import static com.example.bankcards.testConstants.MessageTestConstants.*;
import static com.example.bankcards.testConstants.TokenTestConstants.*;
import static com.example.bankcards.testConstants.UserTestConstants.*;
import static globalConstants.EndpointConstants.*;
import static globalConstants.HeaderConstants.AUTHORIZATION_HEADER;
import static globalConstants.MessageConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Authenticate controller tests")
@ActiveProfiles("test")
class AuthControllerTests {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AuthService authService;
    @Autowired
    private ObjectMapper objectMapper;
    private LoginRequest validLoginRequest;
    private LoginResponse loginResponse;
    private PasswordChangeRequest passwordChangeRequest;

    @BeforeEach
    void setUp() {
        validLoginRequest = new LoginRequest(TEST_USERNAME, TEST_PASSWORD);
        loginResponse = new LoginResponse(
                ACCESS_TOKEN,
                "Bearer",
                1L,
                TEST_USERNAME,
                TEST_EMAIL,
                TEST_FIRST_NAME,
                TEST_LAST_NAME,
                Role.ROLE_USER
        );
        passwordChangeRequest = new PasswordChangeRequest(OLD_PASSWORD, NEW_PASSWORD,
                NEW_PASSWORD);
    }

    @Test
    @DisplayName("Успешная аутентификация с корректными учётными данными")
    void authenticateUserWithValidCredentialsTest() throws Exception {
        when(authService.authenticate(any(LoginRequest.class))).thenReturn(loginResponse);
        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(true))
                .andExpect(jsonPath(JSON_PATH_MESSAGE).value(LOGIN_SUCCESS_MESSAGE))
                .andExpect(jsonPath(JSON_PATH_DATA_TOKEN).value(ACCESS_TOKEN));
        verify(authService).authenticate(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Ошибка при входе с неверными учётными данными")
    void authenticateUserWithInvalidCredentialsTest() throws Exception {
        when(authService.authenticate(any(LoginRequest.class)))
                .thenThrow(new AuthenticationException(INVALID_CREDENTIALS_MESSAGE));
        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(false))
                .andExpect(jsonPath(JSON_PATH_MESSAGE).value(INVALID_CREDENTIALS_MESSAGE));
    }

    @Test
    @DisplayName("Ошибка при входе с пустым именем пользователя")
    void authenticateUserWithEmptyUsernameTest() throws Exception {
        LoginRequest invalidRequest = new LoginRequest("", TEST_PASSWORD);
        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        verify(authService, never()).authenticate(any());
    }

    @Test
    @DisplayName("Успешное обновление токена с валидным refresh-токеном в заголовке")
    void refreshTokenWithValidHeaderTest() throws Exception {
        when(authService.refreshToken(OLD_TOKEN)).thenReturn(loginResponse);
        mockMvc.perform(post(REFRESH_ENDPOINT)
                        .header(AUTHORIZATION_HEADER, BEARER_OLD_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(true))
                .andExpect(jsonPath(JSON_PATH_MESSAGE).value(REFRESH_SUCCESS_MESSAGE));
        verify(authService).refreshToken(OLD_TOKEN);
    }

    @Test
    @DisplayName("Успешная проверка валидности корректного JWT-токена")
    void validateTokenWithValidTokenTest() throws Exception {
        when(authService.validateToken(VALID_TOKEN)).thenReturn(true);
        mockMvc.perform(post(VALIDATE_ENDPOINT)
                        .header(AUTHORIZATION_HEADER, BEARER_VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(true))
                .andExpect(jsonPath(JSON_PATH_MESSAGE).value(VALIDATE_SUCCESS_MESSAGE));
        verify(authService).validateToken(VALID_TOKEN);
    }

    @Test
    @DisplayName("Ошибка при проверке недействительного JWT-токена")
    void validateTokenWithInvalidTokenTest() throws Exception {
        when(authService.validateToken(INVALID_TOKEN)).thenReturn(false);
        mockMvc.perform(post(VALIDATE_ENDPOINT)
                        .header(AUTHORIZATION_HEADER, BEARER_INVALID_TOKEN))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(false))
                .andExpect(jsonPath(JSON_PATH_MESSAGE).value(INVALID_TOKEN_MESSAGE));
        verify(authService).validateToken(INVALID_TOKEN);
    }

    @Test
    @WithMockUser(username = TEST_USERNAME)
    @DisplayName("Успешная смена пароля при верном текущем пароле")
    void changePasswordWithValidRequestTest() throws Exception {
        doNothing().when(authService).changePassword(TEST_USERNAME, OLD_PASSWORD, NEW_PASSWORD);
        mockMvc.perform(post(CHANGE_PASSWORD_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChangeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(true))
                .andExpect(jsonPath(JSON_PATH_MESSAGE).value(CHANGE_PASSWORD_SUCCESS_MESSAGE));
        verify(authService).changePassword(TEST_USERNAME, OLD_PASSWORD, NEW_PASSWORD);
    }

    @Test
    @WithMockUser(username = TEST_USERNAME)
    @DisplayName("Ошибка при смене пароля с неверным текущим паролем")
    void changePasswordWithInvalidCurrentPasswordTest() throws Exception {
        doThrow(new AuthenticationException(INVALID_CURRENT_PASSWORD_MESSAGE))
                .when(authService).changePassword(TEST_USERNAME, WRONG_PASSWORD, NEW_PASSWORD);
        PasswordChangeRequest wrongPasswordRequest = new PasswordChangeRequest(WRONG_PASSWORD,
                NEW_PASSWORD, NEW_PASSWORD);
        mockMvc.perform(post(CHANGE_PASSWORD_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongPasswordRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(false))
                .andExpect(jsonPath(JSON_PATH_MESSAGE).value(INVALID_CURRENT_PASSWORD_MESSAGE));
    }

    @Test
    @DisplayName("Успешный выход из системы с валидным токеном")
    void logoutWithValidTokenTest() throws Exception {
        doNothing().when(authService).logout(VALID_TOKEN);
        mockMvc.perform(post(LOGOUT_ENDPOINT)
                        .header(AUTHORIZATION_HEADER, BEARER_VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(true))
                .andExpect(jsonPath(JSON_PATH_MESSAGE).value(LOGOUT_SUCCESS_MESSAGE));
        verify(authService).logout(VALID_TOKEN);
    }
}