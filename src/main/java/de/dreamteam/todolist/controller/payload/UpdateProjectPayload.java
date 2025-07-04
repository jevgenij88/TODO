package de.dreamteam.todolist.controller.payload;

import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UpdateProjectPayload(

        Long id,
        @Size(max = 255, message = "Das Titelfeld darf nicht länger als 255 Zeichen sein.")
        String title,

        @Size(max = 255, message = "Das Beschreibungsfeld darf nicht länger als 255 Zeichen sein.")
        String description
) {}

