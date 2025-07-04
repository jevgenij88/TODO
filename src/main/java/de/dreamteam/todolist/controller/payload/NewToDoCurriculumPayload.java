package de.dreamteam.todolist.controller.payload;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record NewToDoCurriculumPayload(
        @NotNull(message = "Die ToDo-ID darf nicht null sein.")
        Long todoId,
        @NotNull(message = "Das Startdatum darf nicht null sein.")
        LocalDate startDate,
        @NotNull(message = "Das Enddatum darf nicht null sein.")
        LocalDate endDate
) {}

