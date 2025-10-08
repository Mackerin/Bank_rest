package com.example.bankcards.security;

import com.example.bankcards.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component("cardSecurity")
@RequiredArgsConstructor
public class CardSecurity {
    private final CardRepository cardRepository;

    public boolean isCardOwner(Long cardId, Long userId) {
        return cardRepository.findById(cardId)
                .map(card -> card.getUser().getId().equals(userId))
                .orElse(false);
    }
}