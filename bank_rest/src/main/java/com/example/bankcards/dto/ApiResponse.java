package com.example.bankcards.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    private boolean success;
    private String message;
    private Object data;
    private LocalDateTime timestamp;
    private String path;

    // Успешный ответ
    public static ApiResponse success(String message, Object data, String path) {
        return new ApiResponse(true, message, data, LocalDateTime.now(), path);
    }

    // Ответ с ошибкой
    public static ApiResponse error(String message, String path) {
        return new ApiResponse(false, message, null, LocalDateTime.now(), path);
    }

    // Ответ с ошибкой и дополнительными данными
    public static ApiResponse error(String message, Object data, String path) {
        return new ApiResponse(false, message, data, LocalDateTime.now(), path);
    }
}
