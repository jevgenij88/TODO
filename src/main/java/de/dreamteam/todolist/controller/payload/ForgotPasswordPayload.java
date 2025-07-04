package de.dreamteam.todolist.controller.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordPayload(
        @NotBlank(message = "{user.forgot_password.errors.email_is_empty}")
        @Email(message = "{user.forgot_password.errors.email_is_invalid}")
        String email
) {

}