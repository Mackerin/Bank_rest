package com.example.bankcards.dto;

import com.example.bankcards.entity.CardType;
import com.example.bankcards.entity.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос для поиска карт с пагинацией и фильтрацией")
public class CardSearchRequest {

    @Schema(description = "Часть номера карты для поиска", example = "1234")
    private String cardNumber;

    @Schema(description = "Тип карты", example = "DEBIT")
    private CardType cardType;

    @Schema(description = "Валюта карты", example = "RUB")
    private Currency currency;

    @Schema(description = "Статус активности карты", example = "true")
    private Boolean active;

    @Schema(description = "Статус блокировки карты", example = "false")
    private Boolean isBlocked;

    @Min(value = 0, message = "Номер страницы должен быть не меньше 0")
    @Schema(description = "Номер страницы (начинается с 0)", example = "0", defaultValue = "0")
    private int page = 0;

    @Min(value = 1, message = "Размер страницы должен быть не меньше 1")
    @Schema(description = "Размер страницы", example = "20", defaultValue = "20")
    private int size = 20;

    @Schema(description = "Поле для сортировки (формат: field,asc|desc)", example = "createdAt,desc",
            defaultValue = "createdAt,desc")
    private String sort = "createdAt,desc";
}