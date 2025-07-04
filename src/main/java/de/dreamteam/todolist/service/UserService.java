package de.dreamteam.todolist.service;

import de.dreamteam.todolist.controller.payload.NewUserPayload;
import de.dreamteam.todolist.controller.payload.UpdateUserPayload;
import de.dreamteam.todolist.entity.User;
import de.dreamteam.todolist.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final EmailService emailService;
    private static final int MAX_RESET_ATTEMPTS = 3;
    private static final int MAX_VERIFICATION_ATTEMPTS = 3;


    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public void saveUser(NewUserPayload payload) {
        if (isExpiredVerification(payload.email())) {
            userRepository.deleteByEmail(payload.email());
        }

        User user = User.builder()
                .firstName(payload.firstName())
                .lastName(payload.lastName())
                .username(payload.username())
                .email(payload.email())
                .password(passwordEncoder().encode(payload.password()))
                .build();

        tokenService.setVerificationToken(user);

        User savedUser = userRepository.save(user);

        emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getVerificationToken());
    }

    private boolean isExpiredVerification(String email) {
        User existingUser = userRepository.findUserByEmail(email);
        return existingUser != null && tokenService.isTokenExpired(existingUser.getVerificationTokenExpiry());
    }

    public void updateUser(UpdateUserPayload payload) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User existingUser = userRepository.findUserByUsername(currentUsername);
        if (existingUser == null) {
            return;
        }

        existingUser.setFirstName(payload.firstName());
        existingUser.setLastName(payload.lastName());
        existingUser.setEmail(payload.email());
        existingUser.setUsername(payload.username());

        if (payload.password() != null && !payload.password().isEmpty()) {
            existingUser.setPassword(passwordEncoder().encode(payload.password()));
        }

        userRepository.save(existingUser);
    }

    public void deleteUser() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User existingUser = userRepository.findUserByUsername(currentUsername);
        if (existingUser == null) {
            return;
        }

        userRepository.delete(existingUser);
    }

    public User findUserByUsername(String username) {
        return userRepository.findUserByUsername(username);
    }

    public User findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    @Transactional
    public boolean verifyUser(String token) {
        User user = userRepository.findUserByVerificationToken(token);

        if (user == null || tokenService.isTokenExpired(user.getVerificationTokenExpiry())) {
            return false;
        }

        user.setEnabled(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        user.setVerificationAttempts(null);
        user.setVerificationAttemptsTimestamp(null);
        userRepository.save(user);

        autoLogin(user);

        return true;
    }

    private void autoLogin(User user) {
        org.springframework.security.core.userdetails.User principal =
                new org.springframework.security.core.userdetails.User(
                        user.getUsername(),
                        user.getPassword(),
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Transactional
    public boolean initiatePasswordReset(String email) {
        User user = userRepository.findUserByEmail(email);

        if (user == null) {
            return false;
        }

        if (!checkResetAttempts(user)) {
            return false;
        }

        tokenService.setPasswordResetToken(user);
        userRepository.save(user);

        emailService.sendPasswordResetEmail(user.getEmail(), user.getResetToken());

        return true;
    }

    private boolean checkResetAttempts(User user) {
        LocalDateTime now = LocalDateTime.now();

        if (user.getResetAttemptsTimestamp() == null ||
                now.minusHours(1).isAfter(user.getResetAttemptsTimestamp())) {
            user.setPasswordResetAttempts(1);
            user.setResetAttemptsTimestamp(now);
            return true;
        }

        if (user.getPasswordResetAttempts() >= MAX_RESET_ATTEMPTS) {
            return false;
        }

        user.setPasswordResetAttempts(user.getPasswordResetAttempts() + 1);
        return true;
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        User user = userRepository.findUserByResetToken(token);

        if (user == null || tokenService.isTokenExpired(user.getResetTokenExpiry())) {
            return false;
        }

        user.setPassword(passwordEncoder().encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        user.setPasswordResetAttempts(null);
        userRepository.save(user);

        return true;
    }

    @Transactional
    public boolean initiateVerificationTokenResend(String email) {
        User user = userRepository.findUserByEmail(email);

        if (user == null || tokenService.isTokenExpired(user.getVerificationTokenExpiry())) {
            return false;
        }

        if (!checkVerificationAttempts(user)) {
            return false;
        }

        emailService.sendVerificationEmail(user.getEmail(), user.getVerificationToken());

        return true;
    }

    private boolean checkVerificationAttempts(User user) {
        LocalDateTime now = LocalDateTime.now();

        if (user.getVerificationAttempts() == null ||
                now.minusHours(1).isAfter(user.getVerificationAttemptsTimestamp())) {
            user.setPasswordResetAttempts(1);
            user.setResetAttemptsTimestamp(now);
            return true;
        }

        if (user.getVerificationAttempts() >= MAX_VERIFICATION_ATTEMPTS) {
            return false;
        }

        user.setVerificationAttempts(user.getVerificationAttempts() + 1);
        return true;
    }
}
