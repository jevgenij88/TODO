package de.dreamteam.todolist.controller.payload;

import de.dreamteam.todolist.model.ToDoStatus;

public record UpdateStatusPayload(

        ToDoStatus status

    ) {


}
