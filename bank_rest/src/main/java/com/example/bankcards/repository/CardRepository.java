package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardType;
import com.example.bankcards.entity.Currency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    // Поиск карты по номеру
    Optional<Card> findByCardNumber(String cardNumber);

    // Поиск карт по пользователю
    List<Card> findByUserId(Long userId);

    // Поиск активных карт пользователя
    List<Card> findByUserIdAndActiveTrue(Long userId);

    // Запрос для подсчета общего баланса пользователя
    @Query("SELECT SUM(c.balance) FROM Card c WHERE c.user.id = :userId AND c.active = true")
    Optional<BigDecimal> getTotalBalanceByUserId(@Param("userId") Long userId);

    // Поиск карт пользователя с фильтрами и пагинацией
    @Query("SELECT c FROM Card c WHERE " +
            "c.user.id = :userId AND " +
            "(:cardNumber IS NULL OR c.cardNumber LIKE CONCAT(:cardNumber, '%')) AND " +
            "(:cardType IS NULL OR c.cardType = :cardType) AND " +
            "(:currency IS NULL OR c.currency = :currency) AND " +
            "(:active IS NULL OR c.active = :active) AND " +
            "(:isBlocked IS NULL OR c.isBlocked = :isBlocked)")
    Page<Card> findUserCardsWithFilters(
            @Param("userId") Long userId,
            @Param("cardNumber") String cardNumber,
            @Param("cardType") CardType cardType,
            @Param("currency") Currency currency,
            @Param("active") Boolean active,
            @Param("isBlocked") Boolean isBlocked,
            Pageable pageable
    );
}