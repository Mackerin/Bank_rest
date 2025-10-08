package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ с детальной информацией о статусе карты")
public class CardStatusResponse {

    @Schema(description = "Статус карты", example = "ACTIVE",
            allowableValues = {"ACTIVE", "BLOCKED", "EXPIRED", "DEACTIVATED"})
    private String status;

    @Schema(description = "Детальное описание статуса")
    private String statusDescription;

    @Schema(description = "Активна ли карта", example = "true")
    private Boolean active;

    @Schema(description = "Заблокирована ли карта", example = "false")
    private Boolean blocked;

    @Schema(description = "Истек ли срок действия", example = "false")
    private Boolean expired;

    @Schema(description = "Действительна ли карта (активна, не заблокирована, не просрочена)", example = "true")
    private Boolean valid;

    @Schema(description = "Дата истечения срока действия")
    private LocalDate expiryDate;

    @Schema(description = "Дней до истечения срока действия", example = "365")
    private Long daysUntilExpiry;

    @Schema(description = "Информация о карте")
    private CardDTO card;
}
