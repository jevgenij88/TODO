package de.dreamteam.todolist.controller.payload;


import de.dreamteam.todolist.model.ToDoStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.util.List;


public record NewToDoPayload(

        @NotEmpty
        @Size(min = 5, max = 255)
        String title,

        @NotEmpty
        @Size(min = 5, max = 255)
        String description,

        @FutureOrPresent
        LocalDate startDate,

        @FutureOrPresent
        LocalDate endDate,

        @NotNull
        ToDoStatus status,

        @Nullable
        Long projectId,

        @Nullable
        List<Long> curriculumIds
) {
}
