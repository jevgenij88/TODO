package de.dreamteam.todolist.controller;

import de.dreamteam.todolist.controller.payload.NewToDoPayload;
import de.dreamteam.todolist.controller.payload.UpdateStatusPayload;
import de.dreamteam.todolist.controller.payload.UpdateToDoPayload;
import de.dreamteam.todolist.controller.payload.UserTodoPayload;
import de.dreamteam.todolist.entity.ToDo;
import de.dreamteam.todolist.repository.ToDoRepository;
import de.dreamteam.todolist.service.ToDoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/todo-list-api/todos")
public class ToDoRestController {

    private final ToDoService toDoService;

    private final ToDoRepository toDoRepository;

    @GetMapping
    public List<UserTodoPayload> getAllToDos() {
        try {
            return toDoService.getAllToDos();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @GetMapping("{toDoId:\\d+}")
    public ResponseEntity<ToDo> getToDo(@PathVariable long toDoId) {
        System.out.println("getToDo: " + toDoId);
        return ResponseEntity.status(HttpStatus.OK).body(toDoService.getToDo(toDoId));
    }

    @PostMapping
    public ResponseEntity<NewToDoPayload> createToDo(@Valid @RequestBody NewToDoPayload payload) {
        toDoService.createToDo(payload);
        return ResponseEntity.status(HttpStatus.OK).body(payload);
    }

    @PatchMapping("{toDoId:\\d+}")
    public ResponseEntity<UpdateToDoPayload> updateToDo(@PathVariable Long toDoId, @Valid @RequestBody UpdateToDoPayload payload) {
        try {
            toDoService.updateToDo(payload, toDoId);
            return ResponseEntity.status(HttpStatus.OK).body(payload);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @DeleteMapping("{toDoToDeleteId:\\d+}")
    public ResponseEntity<ToDo> deleteToDo(@PathVariable Long toDoToDeleteId) {

        toDoService.deleteTodo(toDoToDeleteId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("{toDoId:\\d+}/assign/project/{projectId:\\d+}")
    public ResponseEntity<ToDo> assignToProject(@PathVariable Long toDoId, @PathVariable Long projectId){
        ToDo toDo = toDoRepository.findById(toDoId).orElseThrow();
        toDoService.assignToProject(toDo, projectId);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("{toDoId:\\d+}/status")
    public ResponseEntity<UpdateStatusPayload> updateStatus(@PathVariable Long toDoId, @RequestBody UpdateStatusPayload payload) {

        try {
            toDoService.updateStatus(toDoId, payload);
            return ResponseEntity.status(HttpStatus.OK).body(payload);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
