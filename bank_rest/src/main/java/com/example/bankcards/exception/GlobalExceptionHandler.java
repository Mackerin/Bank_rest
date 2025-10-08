package com.example.bankcards.exception;

import com.example.bankcards.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // Обработка ResourceNotFoundException
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        log.warn("Ресурс не найден: {}", ex.getMessage());
        ApiResponse apiResponse = ApiResponse.error(
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    // Обработка InsufficientFundsException
    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ApiResponse> handleInsufficientFundsException(InsufficientFundsException ex, WebRequest request) {
        log.warn("Недостаточно средств: {}", ex.getMessage());
        ApiResponse apiResponse = ApiResponse.error(
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    // Обработка AuthenticationException
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        log.warn("Ошибка аутентификации: {}", ex.getMessage());
        ApiResponse apiResponse = ApiResponse.error(
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }

    // Обработка CardOperationException
    @ExceptionHandler(CardOperationException.class)
    public ResponseEntity<ApiResponse> handleCardOperationException(CardOperationException ex, WebRequest request) {
        log.warn("Ошибка операции с картой: {}", ex.getMessage());
        ApiResponse apiResponse = ApiResponse.error(
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    // Обработка TransactionException
    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<ApiResponse> handleTransactionException(TransactionException ex, WebRequest request) {
        log.warn("Ошибка транзакции: {}", ex.getMessage());
        ApiResponse apiResponse = ApiResponse.error(
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    // Обработка ValidationException
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse> handleValidationException(ValidationException ex, WebRequest request) {
        log.warn("Ошибка валидации: {}", ex.getMessage());
        ApiResponse apiResponse = ApiResponse.error(
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    // Обработка BadCredentialsException
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        log.warn("Неверные учетные данные: {}", ex.getMessage());
        ApiResponse apiResponse = ApiResponse.error(
                "Неверное имя пользователя или пароль",
                request.getDescription(false)
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }

    // Обработка LockedException
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiResponse> handleLockedException(LockedException ex, WebRequest request) {
        log.warn("Аккаунт заблокирован: {}", ex.getMessage());
        ApiResponse apiResponse = ApiResponse.error(
                "Ваш аккаунт временно заблокирован. Попробуйте позже",
                request.getDescription(false)
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.LOCKED);
    }

    // Обработка AccessDeniedException
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        log.warn("Доступ запрещен: {}", ex.getMessage());
        ApiResponse apiResponse = ApiResponse.error(
                "Доступ запрещен. У вас нет прав для выполнения этого действия",
                request.getDescription(false)
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
    }

    // Обработка валидации полей
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("Ошибка валидации полей: {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        ApiResponse apiResponse = ApiResponse.error(
                "Ошибка валидации. Проверьте введенные данные",
                errors,
                request.getDescription(false)
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    // Обработка всех остальных исключений
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGlobalException(Exception ex, WebRequest request) {
        log.error("Внутренняя ошибка сервера: {}", ex.getMessage(), ex);
        ApiResponse apiResponse = ApiResponse.error(
                "Произошла непредвиденная ошибка. Пожалуйста, попробуйте позже",
                request.getDescription(false)
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
