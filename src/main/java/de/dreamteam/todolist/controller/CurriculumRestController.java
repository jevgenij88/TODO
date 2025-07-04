package de.dreamteam.todolist.controller;

import de.dreamteam.todolist.controller.payload.*;
import de.dreamteam.todolist.entity.Curriculum;
import de.dreamteam.todolist.entity.ToDoCurriculum;
import de.dreamteam.todolist.service.CurriculumService;
import de.dreamteam.todolist.service.ToDoCurriculumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/todo-list-api/curriculum")
public class CurriculumRestController {

    private final CurriculumService curriculumService;
    private final ToDoCurriculumService toDoCurriculumService;
    private final MessageSource messageSource;



    @PostMapping
    public ResponseEntity<?> createCurriculum(@Valid @RequestBody NewCurriculumPayload newPayload,
                                                               Locale locale) {
        Curriculum createdCurriculum = curriculumService.createCurriculum(newPayload);
        UpdateCurriculumPayload updatePayload = UpdateCurriculumPayload.builder()
                .id(createdCurriculum.getId())
                .title(createdCurriculum.getTitle())
                .userId(createdCurriculum.getUser().getId())
                .build();

        // Nachricht über die erfolgreiche Löschung
        String successMessage = messageSource.getMessage("curriculum.creation.success", null, locale);

        // Generierung einer Antwort: Projektdaten und Nachricht
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("curriculum", updatePayload);
        responseMap.put("message", successMessage);

        // Zurücksetzen des Status auf 201 Erstellt

        return ResponseEntity.status(HttpStatus.CREATED).body(responseMap);
    }

    @PostMapping("/current/add-todo")
    public ResponseEntity<UpdateToDoCurriculumPayload> addToDoToCurriculum(
            @Valid @RequestBody NewToDoCurriculumPayload request) {

        // Получаем учебный план текущего аутентифицированного пользователя
        Curriculum curriculum = curriculumService.getCurriculumForCurrentUser()
                .orElseThrow(() -> new RuntimeException("Curriculum not found for current user"));

        // Hinzufügen einer Aufgabe zum Lehrplan durch Übergabe der Lehrplan ID des aktuellen Benutzers)
        ToDoCurriculum association = toDoCurriculumService.addToDoToCurriculum(
                curriculum.getId(),
                request.todoId(),
                request.startDate(),
                request.endDate()
        );

        // Zusammenstellung der DTO für die Antwort
        UpdateToDoCurriculumPayload response = UpdateToDoCurriculumPayload.builder()
                .id(association.getId())
                .curriculumId(association.getCurriculum().getId())
                .todoId(association.getToDo().getId())
                .startDate(association.getStartDate())
                .endDate(association.getEndDate())
                .build();

        return ResponseEntity.ok(response);
    }


    @GetMapping("/current")
    public ResponseEntity<?> getCurriculumForCurrentUser(Locale locale) {
        Optional<Curriculum> curriculumOpt = curriculumService.getCurriculumForCurrentUser();
        if (curriculumOpt.isPresent()) {
            Curriculum c = curriculumOpt.get();
            CurriculumResponse response = CurriculumResponse.builder()
                    .id(c.getId())
                    .title(c.getTitle())
                    .userId(c.getUser().getId())
                    .build();
            return ResponseEntity.ok(response);
        } else {
            // Wenn der Lehrplan nicht gefunden wird, kann die Variable (id) nicht verwendet werden, da sie fehlt.
            String errorMsg = messageSource.getMessage("curriculum.not.found", null, locale);
            Map<String, String> errorResponse = Map.of("message", errorMsg);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @GetMapping("/todos")
    public ResponseEntity<List<UpdateToDoCurriculumPayload>> getTasksForCurriculum() {
        // Ermittelt den Lehrplan für den aktuellen Benutzer. Wenn nicht gefunden, wird eine Ausnahme ausgelöst.
        Curriculum curriculum = curriculumService.getCurriculumForCurrentUser()
                .orElseThrow(() -> new RuntimeException("Curriculum not found for current user"));

        // Извлекаем список ассоциаций ToDoCurriculum и маппим их в DTO UpdateToDoCurriculumPayload
        List<UpdateToDoCurriculumPayload> responses = curriculum.getToDoCurriculumList().stream()
                .map(association -> UpdateToDoCurriculumPayload.builder()
                        .id(association.getId())
                        .curriculumId(association.getCurriculum().getId())
                        .todoId(association.getToDo().getId())
                        .startDate(association.getStartDate())
                        .endDate(association.getEndDate())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/current")
    public ResponseEntity<?> updateCurriculum(@Valid @RequestBody UpdateCurriculumPayload updatePayload,
                                              Locale locale) {
        Optional<Curriculum> updatedCurriculumOpt = curriculumService.updateCurriculumForCurrentUser(updatePayload);
        if (updatedCurriculumOpt.isPresent()) {
            Curriculum updatedCurriculum = updatedCurriculumOpt.get();
            CurriculumResponse response = CurriculumResponse.builder()
                    .id(updatedCurriculum.getId())
                    .title(updatedCurriculum.getTitle())
                    .userId(updatedCurriculum.getUser().getId())
                    .build();

            String successMessage = messageSource.getMessage("curriculum.update.success", null, locale);

            Map<String, Object> responseMap = Map.of(
                    "curriculum", response,
                    "message", successMessage
            );

            return ResponseEntity.ok(responseMap);
        } else {
            String errorMsg = messageSource.getMessage("curriculum.not.found", null, locale);
            Map<String, String> errorResponse = Map.of("message", errorMsg);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @PatchMapping("/current/update-todo/{toDoId}")
    public ResponseEntity<UpdateToDoCurriculumPayload> updateToDoDates(
            @PathVariable Long toDoId,
            @Valid @RequestBody NewToDoCurriculumPayload newPayload) {

        // Abrufen des Lehrplans des aktuellen Benutzers
        Curriculum curriculum = curriculumService.getCurriculumForCurrentUser()
                .orElseThrow(() -> new RuntimeException("Curriculum not found for current user"));

        // Verwenden Sie die erhaltene Lehrplan-ID, um das Datum der Aufgabe zu aktualisieren.
        ToDoCurriculum updatedAssociation = toDoCurriculumService.updateToDoCurriculumDates(
                curriculum.getId(),
                toDoId,
                newPayload.startDate(),
                newPayload.endDate()
        );

        UpdateToDoCurriculumPayload response = UpdateToDoCurriculumPayload.builder()
                .id(updatedAssociation.getId())
                .curriculumId(updatedAssociation.getCurriculum().getId())
                .todoId(updatedAssociation.getToDo().getId())
                .startDate(updatedAssociation.getStartDate())
                .endDate(updatedAssociation.getEndDate())
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/current")
    public ResponseEntity<Map<String, Object>> deleteCurriculum(Locale locale) {
        try {
            curriculumService.deleteCurriculumForCurrentUser();
            String successMessage = messageSource.getMessage("curriculum.delete.success", null, locale);
            Map<String, Object> responseMap = Map.of("message", successMessage);
            return ResponseEntity.ok(responseMap);
        } catch (RuntimeException e) {
            String errorMessage = messageSource.getMessage("curriculum.delete.error", null, locale);
            Map<String, Object> errorMap = Map.of("error", errorMessage);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorMap);
        }
    }

    @DeleteMapping("/current/remove-todo/{toDoId}")
    public ResponseEntity<Map<String, Object>> removeTaskFromCurriculum(
            @PathVariable Long toDoId,
            Locale locale) {
        try {
            // Abrufen des Lehrplans des aktuellen Benutzers
            Curriculum curriculum = curriculumService.getCurriculumForCurrentUser()
                    .orElseThrow(() -> new RuntimeException("Curriculum not found for current user"));
            // Удаляем задачу из учебного плана, используя id учебного плана и toDoId
            toDoCurriculumService.removeToDoFromCurriculum(curriculum.getId(), toDoId);
            // Erhalten Sie eine Nachricht über die erfolgreiche Löschung
            String successMessage = messageSource.getMessage("todo.remove.success", null, locale);
            Map<String, Object> responseMap = Map.of("message", successMessage);
            return ResponseEntity.ok(responseMap);
        } catch (Exception e) {
            // Behandeln Sie die Ausnahme und geben Sie eine Fehlermeldung zurück.
            String errorMessage = messageSource.getMessage("todo.remove.error", new Object[]{toDoId}, locale);
            Map<String, Object> errorMap = Map.of("error", errorMessage);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap);
        }
    }
}

