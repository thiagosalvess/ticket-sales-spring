package com.thiagosalvess.ticketsales.features.authentication.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.thiagosalvess.ticketsales.common.exception.NotFoundException;
import com.thiagosalvess.ticketsales.features.user.model.entity.User;
import com.thiagosalvess.ticketsales.features.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {
    private final UserRepository repository;
    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public TokenService(UserRepository repository,
                        @Value("${api.security.token.secret}") String secret) {
        this.repository = repository;

        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret not configured (api.security.token.secret / JWT_SECRET).");
        }
        this.algorithm = Algorithm.HMAC256(secret);
        this.verifier  = JWT.require(algorithm).withIssuer("API Ticket Sales").build();
    }

    public String generateToken(Authentication authentication) {
        String email = authentication.getName();

        Long userId = repository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));

        try {
            return JWT.create()
                    .withIssuer("API Ticket Sales")
                    .withSubject(email)
                    .withClaim("id", userId)
                    .withExpiresAt(expiration())
                    .sign(algorithm);
        } catch (JWTCreationException e) {
            throw new IllegalStateException("Failed to generate JWT", e);
        }
    }

    public String getSubject(String tokenJWT) {
        try {
            return verifier.verify(tokenJWT).getSubject();
        } catch (JWTVerificationException e) {
            throw new BadCredentialsException("Invalid or expired JWT", e);
        }
    }

    public <T> T getClaim(String tokenJWT, String name, Class<T> type) {
        try {
            DecodedJWT jwt = verifier.verify(tokenJWT);
            var claim = jwt.getClaim(name);
            if (claim.isNull()) return null;

            Object v = claim.as(Object.class);

            if (type == Long.class) {
                if (v instanceof Integer i) return type.cast(i.longValue());
                if (v instanceof Long l)    return type.cast(l);
                if (v instanceof Number n)  return type.cast(n.longValue());
            }
            if (type.isInstance(v)) return type.cast(v);

            if (type == String.class)  return type.cast(claim.asString());
            if (type == Integer.class) return type.cast(claim.asInt());
            if (type == Boolean.class) return type.cast(claim.asBoolean());

            throw new IllegalArgumentException("Invalid claim type for " + name);
        } catch (JWTVerificationException e) {
            throw new BadCredentialsException("Invalid or expired JWT", e);
        }
    }

    private Instant expiration() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.UTC);
    }
}
