package com.example.bankcards.dto;

import com.example.bankcards.entity.CardType;
import com.example.bankcards.entity.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CardDTO {
    private Long id;
    private String cardNumber;
    private String cardHolderName;
    private LocalDate expiryDate;
    private CardType cardType;
    private Currency currency;
    private BigDecimal balance;
    private BigDecimal creditLimit;
    private BigDecimal availableCredit;
    private Boolean active;
    private Boolean isBlocked;
    private LocalDateTime createdAt;
    private Long userId;
    private String userFullName;
}
