package de.dreamteam.todolist.controller.payload;

import jakarta.validation.constraints.NotEmpty;

public record LoginPayload(

        @NotEmpty(message = "{}")
        String username,

        @NotEmpty(message = "{}")
        String password
) {
}
