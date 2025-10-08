package com.example.bankcards.service;

import com.example.bankcards.dto.LoginRequest;
import com.example.bankcards.dto.LoginResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.AuthenticationException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static globalConstants.MessageConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse authenticate(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            String jwt = jwtTokenProvider.generateToken(authentication);

            return new LoginResponse(
                    jwt,
                    "Bearer",
                    userPrincipal.getId(),
                    userPrincipal.getUsername(),
                    userPrincipal.getEmail(),
                    userPrincipal.getUser().getFirstName(),
                    userPrincipal.getUser().getLastName(),
                    userPrincipal.getUser().getRole()
            );

        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new AuthenticationException(AUTH_INVALID_CREDENTIALS_MESSAGE);
        }
    }

    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new AuthenticationException(AUTH_CURRENT_PASSWORD_INCORRECT_MESSAGE);
        }

        if (newPassword.length() < 8) {
            throw new AuthenticationException(AUTH_NEW_PASSWORD_TOO_SHORT_MESSAGE);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    public LoginResponse refreshToken(String oldToken) {
        if (jwtTokenProvider.validateToken(oldToken)) {
            String username = jwtTokenProvider.getUsernameFromToken(oldToken);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException(username));

            UserPrincipal userPrincipal = new UserPrincipal(user);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userPrincipal, null, userPrincipal.getAuthorities()
            );

            String newToken = jwtTokenProvider.generateToken(authentication);

            return new LoginResponse(
                    newToken,
                    "Bearer",
                    userPrincipal.getId(),
                    userPrincipal.getUsername(),
                    userPrincipal.getEmail(),
                    userPrincipal.getUser().getFirstName(),
                    userPrincipal.getUser().getLastName(),
                    userPrincipal.getUser().getRole()
            );
        }
        throw new AuthenticationException(AUTH_INVALID_TOKEN_MESSAGE);
    }

    public void logout(String token) {
        log.info("Пользователь вышел из системы, токен: {}", token.substring(0, Math.min(token.length(), 10)) + "...");
        SecurityContextHolder.clearContext();
    }
}