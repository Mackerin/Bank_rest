package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос для перевода между собственными картами")
public class OwnCardsTransferRequest {

    @NotNull(message = "ID карты отправителя обязателен")
    @Schema(description = "ID карты отправителя", example = "1", required = true)
    private Long fromCardId;

    @NotNull(message = "ID карты получателя обязателен")
    @Schema(description = "ID карты получателя", example = "2", required = true)
    private Long toCardId;

    @NotNull(message = "Сумма перевода обязательна")
    @Positive(message = "Сумма перевода должна быть положительной")
    @Schema(description = "Сумма перевода", example = "1000.00", required = true)
    private BigDecimal amount;

    @Schema(description = "Описание перевода", example = "Перевод между своими картами")
    private String description;
}
