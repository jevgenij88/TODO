package de.dreamteam.todolist.service;

import de.dreamteam.todolist.entity.User;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class TokenService {

    private static final int TOKEN_LENGTH = 30;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    public String generateToken() {
        byte[] randomBytes = new byte[TOKEN_LENGTH];
        SECURE_RANDOM.nextBytes(randomBytes);
        return ENCODER.encodeToString(randomBytes);
    }

    public void setVerificationToken(User user) {
        String token = generateToken();
        user.setVerificationToken(token);
        user.setVerificationTokenExpiry(LocalDateTime.now().plusDays(1));
        user.setEnabled(false);
    }

    public void setPasswordResetToken(User user) {
        String token = generateToken();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
    }

    public boolean isTokenExpired(LocalDateTime expiryDate) {
        return expiryDate == null || LocalDateTime.now().isAfter(expiryDate);
    }
}