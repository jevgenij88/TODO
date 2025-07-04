package de.dreamteam.todolist.controller.payload;

import lombok.Builder;

@Builder
public record CurriculumResponse(
        Long id,
        String title,
        Long userId
) {}
