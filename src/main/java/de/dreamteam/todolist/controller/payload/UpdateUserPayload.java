package de.dreamteam.todolist.controller.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserPayload(

        @NotEmpty(message = "{user.update.errors.first_name_is_empty}")
        @Size(min = 3, max = 255, message = "{user.update.errors.first_name_is_invalid}")
        String firstName,

        @NotEmpty(message = "{user.update.errors.last_name_is_empty}")
        @Size(min = 3, max = 255, message = "{user.update.errors.last_name_is_invalid}")
        String lastName,

        @NotEmpty(message = "{user.update.errors.username_is_empty}")
        @Size(min = 5, max = 255, message = "{user.update.errors.username_is_invalid}")
        String username,

        @NotEmpty(message = "{user.update.errors.email_is_empty}")
        @Email(message = "{user.update.errors.email_is_invalid}")
        String email,

        @Pattern(regexp = "^$|^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z0-9]{8,}$",
                message = "{user.update.errors.password_is_invalid}")
        String password) {
}
