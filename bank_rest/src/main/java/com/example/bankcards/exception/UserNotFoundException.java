package com.example.bankcards.exception;

public class UserNotFoundException extends ResourceNotFoundException {

    public UserNotFoundException(Long userId) {
        super("Пользователь", "id", userId);
    }

    public UserNotFoundException(String username) {
        super("Пользователь", "username", username);
    }
}
