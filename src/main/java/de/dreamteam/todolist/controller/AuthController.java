package de.dreamteam.todolist.controller;

import de.dreamteam.todolist.controller.payload.ForgotPasswordPayload;
import de.dreamteam.todolist.controller.payload.LoginPayload;
import de.dreamteam.todolist.controller.payload.ResetPasswordPayload;
import de.dreamteam.todolist.entity.User;
import de.dreamteam.todolist.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("todo-list-api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final SecurityContextRepository securityContextRepository;
    private final MessageSource messageSource;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginPayload payload, Locale locale,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {

        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(payload.username(), payload.password());

        Authentication authentication = authenticationManager.authenticate(authRequest);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(context, request, response);

        User user = userService.findUserByUsername(payload.username());

        Map<String, Object> resp = new HashMap<>();
        resp.put("username", user.getUsername());
        resp.put("message", messageSource.getMessage("auth.login.info.login_is_successful",
                null, locale));

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser(Locale locale) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getName())) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", messageSource.getMessage("auth.current.errors.user_not_authenticated",
                    null, locale));
            return ResponseEntity.status(401).body(errorResponse);
        }

        User user = userService.findUserByUsername(authentication.getName());

        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", messageSource.getMessage(
                    "auth.current.errors.user_not_found", null, locale)));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("email", user.getEmail());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, Locale locale) {
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(null);
        SecurityContextHolder.clearContext();

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", messageSource.getMessage(
                "auth.logout.info.logout_is_successful", null, locale));

        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordPayload payload, Locale locale) {
        boolean success = userService.initiatePasswordReset(payload.email());

        Map<String, String> response = new HashMap<>();
        if (success) {
            response.put("message", messageSource.getMessage(
                    "auth.forgot.info.email_sent", null, locale));
            return ResponseEntity.ok(response);
        } else {
            response.put("error", messageSource.getMessage(
                    "auth.forgot.errors.too_many_attempts", null, locale));
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordPayload payload, Locale locale) {
        boolean success = userService.resetPassword(payload.token(), payload.password());

        Map<String, String> response = new HashMap<>();
        if (success) {
            response.put("message", messageSource.getMessage(
                    "auth.reset.info.password_reset_successful", null, locale));
            return ResponseEntity.ok(response);
        } else {
            response.put("error", messageSource.getMessage(
                    "auth.reset.errors.invalid_token", null, locale));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token, Locale locale,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        boolean success = userService.verifyUser(token);

        Map<String, String> responseBody = new HashMap<>();
        if (success) {
            SecurityContext context = SecurityContextHolder.getContext();
            securityContextRepository.saveContext(context, request, response);

            responseBody.put("message", messageSource.getMessage(
                    "auth.verify.info.verification_successful", null, locale));
            return ResponseEntity.ok(responseBody);
        } else {
            responseBody.put("error", messageSource.getMessage(
                    "auth.verify.errors.invalid_token", null, locale));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
        }
    }

    @GetMapping("/resend-verification-token")
    public ResponseEntity<?> resendVerificationToken(@RequestParam String email, Locale locale) {
        boolean success = userService.initiateVerificationTokenResend(email);

        Map<String, String> response = new HashMap<>();
        if (success) {
            response.put("message", messageSource.getMessage(
                    "auth.resend_verification_token.info.email_sent", null, locale));
            return ResponseEntity.ok(response);
        } else {
            response.put("error", messageSource.getMessage(
                    "auth.resend_verification_token.errors.too_many_attempts_or_token_expired", null, locale));
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
        }
    }
}
