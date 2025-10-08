package com.example.bankcards.dto;

import com.example.bankcards.entity.CardType;
import com.example.bankcards.entity.Currency;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCardRequest {
    @NotNull(message = "Тип карты обязателен")
    private CardType cardType;

    @NotNull(message = "Валюта обязательна")
    private Currency currency;

    @NotNull(message = "ID пользователя обязателен")
    private Long userId;
}
