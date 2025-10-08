package com.example.bankcards.service;

import com.example.bankcards.dto.LoginRequest;
import com.example.bankcards.dto.LoginResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.AuthenticationException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static com.example.bankcards.testConstants.TokenTestConstants.*;
import static com.example.bankcards.testConstants.UserTestConstants.*;
import static globalConstants.MessageConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auth Service tests")
class AuthServiceTests {
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private AuthService authService;
    private User testUser;
    private UserPrincipal testUserPrincipal;
    private LoginRequest validLoginRequest;
    private LoginRequest invalidLoginRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username(TEST_USERNAME)
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .firstName(TEST_FIRST_NAME)
                .lastName(TEST_LAST_NAME)
                .role(Role.ROLE_USER)
                .active(true)
                .build();

        testUserPrincipal = new UserPrincipal(testUser);

        validLoginRequest = new LoginRequest(TEST_USERNAME, TEST_PASSWORD);
        invalidLoginRequest = new LoginRequest(TEST_USERNAME, WRONG_PASSWORD);

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Успешная аутентификация с корректными учётными данными")
    void authenticateWithValidCredentialsTest() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUserPrincipal);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn(ACCESS_TOKEN);
        LoginResponse response = authService.authenticate(validLoginRequest);
        assertNotNull(response);
        assertEquals(ACCESS_TOKEN, response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals(1L, response.getId());
        assertEquals(TEST_USERNAME, response.getUsername());
        assertEquals(TEST_EMAIL, response.getEmail());
        assertEquals(TEST_FIRST_NAME, response.getFirstName());
        assertEquals(TEST_LAST_NAME, response.getLastName());
        assertEquals(Role.ROLE_USER, response.getRole());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider).generateToken(authentication);
    }

    @Test
    @DisplayName("Ошибка при аутентификации с неверным паролем")
    void authenticateWithInvalidCredentialsTest() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Неверные учетные данные"));
        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> authService.authenticate(invalidLoginRequest));
        assertEquals(AUTH_INVALID_CREDENTIALS_MESSAGE, exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Успешная смена пароля при верном текущем пароле")
    void changePasswordWithValidDataTest() {
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(OLD_PASSWORD, TEST_PASSWORD)).thenReturn(true);
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(ENCODE_PASSWORD);
        authService.changePassword(TEST_USERNAME, OLD_PASSWORD, NEW_PASSWORD);
        verify(userRepository).findByUsername(TEST_USERNAME);
        verify(passwordEncoder).matches(OLD_PASSWORD, TEST_PASSWORD);
        verify(passwordEncoder).encode(NEW_PASSWORD);
        verify(userRepository).save(testUser);
        assertEquals(ENCODE_PASSWORD, testUser.getPassword());
    }

    @Test
    @DisplayName("Ошибка при смене пароля с неверным текущим паролем")
    void changePasswordWithInvalidCurrentPasswordTest() {
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(WRONG_PASSWORD, TEST_PASSWORD)).thenReturn(false);
        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> authService.changePassword(TEST_USERNAME, WRONG_PASSWORD, NEW_PASSWORD));
        assertEquals(AUTH_CURRENT_PASSWORD_INCORRECT_MESSAGE, exception.getMessage());
        verify(userRepository).findByUsername(TEST_USERNAME);
        verify(passwordEncoder).matches(WRONG_PASSWORD, TEST_PASSWORD);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Ошибка при смене пароля для несуществующего пользователя")
    void changePasswordWithNonExistentUserTest() {
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> authService.changePassword(TEST_USERNAME, OLD_PASSWORD, NEW_PASSWORD));
        assertTrue(exception.getMessage().contains(TEST_USERNAME));
        verify(userRepository).findByUsername(TEST_USERNAME);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Ошибка при попытке установить слишком короткий пароль")
    void changePasswordWithShortNewPasswordTest() {
        String shortPassword = "1234567";
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(OLD_PASSWORD, TEST_PASSWORD)).thenReturn(true);
        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> authService.changePassword(TEST_USERNAME, OLD_PASSWORD, shortPassword));
        assertEquals(AUTH_NEW_PASSWORD_TOO_SHORT_MESSAGE, exception.getMessage());
        verify(userRepository).findByUsername(TEST_USERNAME);
        verify(passwordEncoder).matches(OLD_PASSWORD, TEST_PASSWORD);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Валидный JWT-токен успешно проходит проверку")
    void validateTokenWithValidTokenTest() {
        when(jwtTokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
        boolean isValid = authService.validateToken(VALID_TOKEN);
        assertTrue(isValid);
        verify(jwtTokenProvider).validateToken(VALID_TOKEN);
    }

    @Test
    @DisplayName("Недействительный JWT-токен не проходит проверку")
    void validateTokenWithInvalidTokenTest() {
        when(jwtTokenProvider.validateToken(INVALID_TOKEN)).thenReturn(false);
        boolean isValid = authService.validateToken(INVALID_TOKEN);
        assertFalse(isValid);
        verify(jwtTokenProvider).validateToken(INVALID_TOKEN);
    }

    @Test
    @DisplayName("Успешное обновление access-токена по валидному refresh-токену")
    void refreshTokenWithValidTokenTest() {
        String newToken = "new.jwt.token";
        when(jwtTokenProvider.validateToken(OLD_TOKEN)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(OLD_TOKEN)).thenReturn(TEST_USERNAME);
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn(newToken);
        LoginResponse response = authService.refreshToken(OLD_TOKEN);
        assertNotNull(response);
        assertEquals(newToken, response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals(1L, response.getId());
        assertEquals(TEST_USERNAME, response.getUsername());
        assertEquals(TEST_EMAIL, response.getEmail());
        assertEquals(TEST_FIRST_NAME, response.getFirstName());
        assertEquals(TEST_LAST_NAME, response.getLastName());
        assertEquals(Role.ROLE_USER, response.getRole());
        verify(jwtTokenProvider).validateToken(OLD_TOKEN);
        verify(jwtTokenProvider).getUsernameFromToken(OLD_TOKEN);
        verify(userRepository).findByUsername(TEST_USERNAME);
        verify(jwtTokenProvider).generateToken(any(Authentication.class));
    }

    @Test
    @DisplayName("Ошибка при обновлении токена с недействительным refresh-токеном")
    void refreshTokenWithInvalidTokenTest() {
        when(jwtTokenProvider.validateToken(INVALID_TOKEN)).thenReturn(false);
        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> authService.refreshToken(INVALID_TOKEN));
        assertEquals(AUTH_INVALID_TOKEN_MESSAGE, exception.getMessage());
        verify(jwtTokenProvider).validateToken(INVALID_TOKEN);
        verify(jwtTokenProvider, never()).getUsernameFromToken(anyString());
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    @DisplayName("Ошибка при обновлении токена для несуществующего пользователя")
    void refreshTokenWithNonExistentUserTest() {
        when(jwtTokenProvider.validateToken(OLD_TOKEN)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(OLD_TOKEN)).thenReturn(TEST_USERNAME);
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> authService.refreshToken(OLD_TOKEN));
        assertTrue(exception.getMessage().contains(TEST_USERNAME));
        verify(jwtTokenProvider).validateToken(OLD_TOKEN);
        verify(jwtTokenProvider).getUsernameFromToken(OLD_TOKEN);
        verify(userRepository).findByUsername(TEST_USERNAME);
        verify(jwtTokenProvider, never()).generateToken(any(Authentication.class));
    }

    @Test
    @DisplayName("При выходе из системы контекст безопасности успешно очищается")
    void logoutTest() {
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        authService.logout(VALID_TOKEN);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}