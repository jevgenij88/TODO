package de.dreamteam.todolist.controller;

import de.dreamteam.todolist.controller.payload.NewUserPayload;
import de.dreamteam.todolist.controller.payload.UpdateUserPayload;
import de.dreamteam.todolist.entity.User;
import de.dreamteam.todolist.repository.UserRepository;
import de.dreamteam.todolist.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@EnableMethodSecurity
@RequestMapping("todo-list-api/users")
public class UserRestController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final MessageSource messageSource;

    @GetMapping
    public User findUser(String username) {
        return this.userRepository.findUserByUsername(username);
    }

    @PreAuthorize("isAnonymous()")
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody NewUserPayload payload,
                                        BindingResult bindingResult)
            throws BindException {
        if (bindingResult.hasErrors()) {
            if (bindingResult instanceof BindException exception) {
                throw exception;
            } else {
                throw new BindException(bindingResult);
            }
        } else {
            this.userService.saveUser(payload);
            return ResponseEntity.noContent()
                    .build();
        }
    }

    @PatchMapping
    public ResponseEntity<?> updateUser(@Valid @RequestBody UpdateUserPayload payload,
                                        BindingResult bindingResult)
            throws BindException {
        if (bindingResult.hasErrors()) {
            if (bindingResult instanceof BindException exception) {
                throw exception;
            } else {
                throw new BindException(bindingResult);
            }
        } else {
            this.userService.updateUser(payload);
            return ResponseEntity.noContent()
                    .build();
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteProduct() {
        this.userService.deleteUser();
        return ResponseEntity.noContent()
                .build();
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ProblemDetail> handleNoSuchElementException(NoSuchElementException exception,
                                                                      Locale locale) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                        this.messageSource.getMessage(exception.getMessage(), new Object[0],
                                exception.getMessage(), locale)));
    }
}
