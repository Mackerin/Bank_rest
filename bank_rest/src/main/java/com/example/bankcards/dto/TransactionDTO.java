package com.example.bankcards.dto;

import com.example.bankcards.entity.TransactionStatus;
import com.example.bankcards.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private Long id;
    private String transactionId;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionStatus status;
    private String description;
    private BigDecimal commission;
    private LocalDateTime createdAt;

    private Long fromUserId;
    private String fromUserFullName;
    private Long fromCardId;
    private String fromCardNumber;

    private Long toUserId;
    private String toUserFullName;
    private Long toCardId;
    private String toCardNumber;

    public BigDecimal getTotalAmount() {
        return amount.add(commission != null ? commission : BigDecimal.ZERO);
    }
}
