package de.dreamteam.todolist.service;

import de.dreamteam.todolist.entity.Curriculum;
import de.dreamteam.todolist.entity.ToDo;
import de.dreamteam.todolist.entity.ToDoCurriculum;
import de.dreamteam.todolist.repository.CurriculumRepository;
import de.dreamteam.todolist.repository.ToDoCurriculumRepository;
import de.dreamteam.todolist.repository.ToDoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ToDoCurriculumService {

    private final ToDoCurriculumRepository toDoCurriculumRepository;
    private final CurriculumRepository curriculumRepository;
    private final ToDoRepository toDoRepository;


    // Fügen Sie dem Lehrplan eine Aufgabe mit den angegebenen Daten hinzu.

    public ToDoCurriculum addToDoToCurriculum(Long curriculumId, Long toDoId, LocalDate startDate, LocalDate endDate) {
        // Curriculum abrufen, wenn nicht gefunden, Ausnahme auslösen
        Curriculum curriculum = curriculumRepository.findById(curriculumId)
                .orElseThrow(() -> new RuntimeException("Curriculum not found with id: " + curriculumId));

        // ToDo abrufen, wenn nicht gefunden, Ausnahme auslösen
        ToDo toDo = toDoRepository.findById(toDoId)
                .orElseThrow(() -> new RuntimeException("ToDo not found with id: " + toDoId));

        // Erstellen Sie eine Assoziation zwischen Curriculum und ToDo
        ToDoCurriculum association = ToDoCurriculum.builder()
                .curriculum(curriculum)
                .toDo(toDo)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        // Aktualisierung der bidirektionalen Kommunikation (falls für die Anwendungslogik erforderlich)
        curriculum.getToDoCurriculumList().add(association);
        toDo.getToDoCurriculumList().add(association);

        // Speichern Sie die Assoziation
        return toDoCurriculumRepository.save(association);
    }

    public ToDoCurriculum updateToDoCurriculumDates(Long curriculumId, Long toDoId, LocalDate newStartDate, LocalDate newEnDate) {
        ToDoCurriculum association = toDoCurriculumRepository
                .findByCurriculum_IdAndToDo_Id(curriculumId, toDoId)
                .orElseThrow(() -> new RuntimeException("Association not found for curriculum id " + curriculumId + " and todo id " + toDoId));

        association.setStartDate(newStartDate);
        association.setEndDate(newEnDate);

        return toDoCurriculumRepository.save(association);
    }

    public void removeToDoFromCurriculum(Long curriculumId, Long toDoId) {
        Optional<ToDoCurriculum> associationOpt = toDoCurriculumRepository
                .findByCurriculum_IdAndToDo_Id(curriculumId, toDoId);
        if (associationOpt.isPresent()) {
            toDoCurriculumRepository.delete(associationOpt.get());
        } else {
            throw new RuntimeException("Association not found for curriculum id " + curriculumId + " and todo id " + toDoId);
        }
    }
}

