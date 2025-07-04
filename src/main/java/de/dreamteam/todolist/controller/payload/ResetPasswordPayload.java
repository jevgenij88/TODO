package de.dreamteam.todolist.controller.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResetPasswordPayload(
        @NotBlank(message = "{user.reset_password.errors.token_is_empty}")
        String token,

        @NotBlank(message = "{user.reset_password.errors.password_is_empty}")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z0-9]{8,}$",
                message = "{user.reset_password.errors.password_is_invalid}")
        String password
) {}
