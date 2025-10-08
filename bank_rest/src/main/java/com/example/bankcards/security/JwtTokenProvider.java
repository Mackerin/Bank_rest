package com.example.bankcards.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {
    @Value("${app.jwt.secret:}")
    private String jwtSecret;
    @Value("${app.jwt.expiration:86400000}")
    private int jwtExpirationInMs;
    @Value("${app.jwt.issuer:bank-cards-app}")
    private String jwtIssuer;

    private SecretKey getSigningKey() {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException("Секретный ключ JWT не настроен");
        }
        if (jwtSecret.length() < 32) {
            throw new IllegalStateException("Секретный ключ JWT должен быть длиной не менее 32 символов");
        }
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .claim("id", userPrincipal.getId())
                .claim("email", userPrincipal.getEmail())
                .claim("authorities", authorities)
                .setIssuer(jwtIssuer)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("id", Long.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException ex) {
            log.error("Неверная подпись JWT: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Неверный JWT-токен: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Просроченный JWT-токен: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Неподдерживаемый JWT-токен: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("Строка claims JWT пуста: {}", ex.getMessage());
        } catch (JwtException ex) {
            log.error("Ошибка проверки JWT: {}", ex.getMessage());
        }
        return false;
    }
}
