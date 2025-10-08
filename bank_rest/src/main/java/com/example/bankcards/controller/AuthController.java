package com.example.bankcards.controller;

import com.example.bankcards.dto.ApiResponse;
import com.example.bankcards.dto.LoginRequest;
import com.example.bankcards.dto.LoginResponse;
import com.example.bankcards.dto.PasswordChangeRequest;
import com.example.bankcards.exception.AuthenticationException;
import com.example.bankcards.exception.ValidationException;
import com.example.bankcards.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static globalConstants.EndpointConstants.*;
import static globalConstants.HeaderConstants.AUTHORIZATION_HEADER;
import static globalConstants.HeaderConstants.BEARER_HEADER;
import static globalConstants.MessageConstants.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "API для аутентификации и управления доступом")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Вход в систему", description = "Аутентификация пользователя и получение JWT токена")
    public ResponseEntity<ApiResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.authenticate(loginRequest);
        return ResponseEntity.ok(ApiResponse.success(LOGIN_SUCCESS_MESSAGE, loginResponse,
                LOGIN_ENDPOINT));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Обновление токена", description = "Получение нового JWT токена по старому")
    public ResponseEntity<ApiResponse> refreshToken(@RequestHeader(AUTHORIZATION_HEADER) String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_HEADER)) {
            throw new ValidationException(AUTHORIZATION_HEADER_INVALID_FORMAT);
        }
        String oldToken = authorizationHeader.substring(7);
        LoginResponse response = authService.refreshToken(oldToken);
        return ResponseEntity.ok(ApiResponse.success(REFRESH_SUCCESS_MESSAGE, response,
                REFRESH_ENDPOINT));
    }

    @PostMapping("/validate")
    @Operation(summary = "Проверка токена", description = "Проверка валидности JWT токена")
    public ResponseEntity<ApiResponse> validateToken(@RequestHeader(AUTHORIZATION_HEADER) String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_HEADER)) {
            throw new ValidationException(AUTHORIZATION_HEADER_INVALID_FORMAT);
        }
        String token = authorizationHeader.substring(7);
        boolean isValid = authService.validateToken(token);
        if (isValid) {
            return ResponseEntity.ok(ApiResponse.success(VALIDATE_SUCCESS_MESSAGE, null,
                    VALIDATE_ENDPOINT));
        } else {
            throw new AuthenticationException(INVALID_TOKEN_MESSAGE);
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(
            @Valid @RequestBody PasswordChangeRequest passwordChangeRequest,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        String username = userDetails.getUsername();
        authService.changePassword(username, passwordChangeRequest.getCurrentPassword(),
                passwordChangeRequest.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success(CHANGE_PASSWORD_SUCCESS_MESSAGE, null,
                CHANGE_PASSWORD_ENDPOINT));
    }

    @PostMapping("/logout")
    @Operation(summary = "Выход из системы", description = "Завершение сессии пользователя")
    public ResponseEntity<ApiResponse> logout(@RequestHeader(AUTHORIZATION_HEADER) String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_HEADER)) {
            String token = authorizationHeader.substring(7);
            authService.logout(token);
        }
        return ResponseEntity.ok(ApiResponse.success(LOGOUT_SUCCESS_MESSAGE, null,
                LOGOUT_ENDPOINT));
    }
}