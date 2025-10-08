package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Поиск пользователя по username
    Optional<User> findByUsername(String username);

    // Проверка существования пользователя по username
    Boolean existsByUsername(String username);

    // Проверка существования пользователя по email
    Boolean existsByEmail(String email);

    // Поиск пользователей по роли
    List<User> findByRole(com.example.bankcards.entity.Role role);
}