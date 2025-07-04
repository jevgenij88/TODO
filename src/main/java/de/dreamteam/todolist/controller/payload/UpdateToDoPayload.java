package de.dreamteam.todolist.controller.payload;

import de.dreamteam.todolist.model.ToDoStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.util.List;

public record UpdateToDoPayload(

        Long id,

        String title,

        String description,

//        String creator,

        @FutureOrPresent
        LocalDate endDate,

        LocalDate startDate,

        ToDoStatus status,

        @Nullable
        Long projectId,

        List<Long> curriculumIds,

        List<Long> userIds
) {
}
