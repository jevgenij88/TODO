package de.dreamteam.todolist.service;


import de.dreamteam.todolist.controller.payload.NewToDoPayload;
import de.dreamteam.todolist.controller.payload.UpdateStatusPayload;
import de.dreamteam.todolist.controller.payload.UpdateToDoPayload;
import de.dreamteam.todolist.controller.payload.UserTodoPayload;
import de.dreamteam.todolist.entity.*;
import de.dreamteam.todolist.repository.CurriculumRepository;
import de.dreamteam.todolist.repository.ProjectRepository;
import de.dreamteam.todolist.repository.ToDoCurriculumRepository;
import de.dreamteam.todolist.repository.ToDoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toList;


@Slf4j
@Service
@RequiredArgsConstructor
public class ToDoService {

    private final ToDoRepository toDoRepository;
    private final ProjectRepository projectRepository;
    private final CurriculumRepository curriculumRepository;
    private final ToDoCurriculumRepository toDoCurriculumRepository;
    private final UserService userService;

    public void createToDo(NewToDoPayload payload) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = userService.findUserByUsername(authentication.getName()).getId();
        List<User> users = new ArrayList<>();
        users.add(userService.getUserById(userId));

        ToDo toDo = ToDo.builder()
                .creator(userService.getUserById(userId).getUsername())
                .title(payload.title())
                .description(payload.description())
                .endDate(payload.endDate())
                .startDate(payload.startDate())
                .status(payload.status())
                .userList(users)
                .build();

        if (payload.projectId() != null) {
            Project project = projectRepository.findById(payload.projectId()).orElseThrow();
            toDo.setProject(project);
        }

        toDoRepository.save(toDo);
    }

    public List<UserTodoPayload> getAllToDos() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = userService.findUserByUsername(authentication.getName()).getId();

        return toDoRepository.findAll()
                .stream().filter(toDo -> toDo.getUserList().stream().anyMatch(user -> user.getId().equals(userId))).map(toDo ->
                        new UserTodoPayload(
                                toDo.getId(),
                                toDo.getTitle(),
                                toDo.getCreator(),
                                toDo.getDescription(),
                                toDo.getStartDate(),
                                toDo.getEndDate(),
                                toDo.getStatus(),
                                toDo.getProject() != null ? toDo.getProject().getId() : null,
                                toDo.getToDoCurriculumList().stream().map(toDoCurriculum -> toDoCurriculum.getCurriculum().getId()).collect(toList())
                        )
                ).toList();
    }


    public void updateToDo(UpdateToDoPayload payload, Long todoId) {

        ToDo existingToDo = toDoRepository.findById(todoId).orElseThrow();

        existingToDo.setTitle(payload.title() != null ? payload.title() : existingToDo.getTitle());
        existingToDo.setDescription(payload.description() != null ? payload.description() : existingToDo.getDescription());
        existingToDo.setEndDate(payload.endDate() != null ? payload.endDate() : existingToDo.getEndDate());
        existingToDo.setStartDate(payload.startDate() != null ? payload.startDate() : existingToDo.getStartDate());
        existingToDo.setStatus(payload.status() != null ? payload.status() : existingToDo.getStatus());
        if (payload.projectId() != null) {
            Project project = projectRepository.findById(payload.projectId()).orElseThrow();
            existingToDo.setProject(project);
        } else {
            existingToDo.setProject(null);
        }
        assignToCurriculum(existingToDo, payload);
        toDoRepository.save(existingToDo);
    }

    public void assignToProject(ToDo toDo, Long projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        project.getToDos().add(toDo);
        toDo.setProject(project);
        toDoRepository.save(toDo);
    }

    public void assignToCurriculum(ToDo existingToDo, UpdateToDoPayload payload) {
        List<ToDoCurriculum> existingToDoCurriculums = toDoCurriculumRepository.findAll();

        if (payload.curriculumIds() != null && !payload.curriculumIds().isEmpty()) {

            List<Curriculum> curriculums = curriculumRepository.findAllById(payload.curriculumIds());
            List<ToDoCurriculum> toDoCurriculumList = new ArrayList<>();

            for (Curriculum curriculum : curriculums) {

                boolean tcAlreadyExists = existingToDoCurriculums.stream().anyMatch(toDoCurriculum ->
                        Objects.equals(toDoCurriculum.getCurriculum().getId(), curriculum.getId()) &&
                                Objects.equals(toDoCurriculum.getToDo().getId(), existingToDo.getId()));

                if (!tcAlreadyExists) {

                    ToDoCurriculum toDoCurriculum = new ToDoCurriculum();
                    toDoCurriculum.setToDo(existingToDo);
                    toDoCurriculum.setCurriculum(curriculum);
                    toDoCurriculum.setStartDate(existingToDo.getStartDate());
                    toDoCurriculum.setEndDate(existingToDo.getEndDate());
                    toDoCurriculumList.add(toDoCurriculum);
                    toDoCurriculumRepository.save(toDoCurriculum);
                } else {
                    List<ToDoCurriculum> toDelete = existingToDoCurriculums.stream().filter(toDoCurriculum ->
                            Objects.equals(toDoCurriculum.getToDo().getId(), existingToDo.getId()) &&
                                    !Objects.equals(toDoCurriculum.getCurriculum().getId(), curriculum.getId())).toList();
                    toDoCurriculumRepository.deleteAll(toDelete);
                }
            }
            existingToDo.setToDoCurriculumList(toDoCurriculumList);
        } else {
            List<ToDoCurriculum> toDelete = existingToDoCurriculums.stream()
                    .filter(toDoCurriculum -> Objects.equals(toDoCurriculum.getToDo().getId(), existingToDo.getId()))
                    .toList();
            toDoCurriculumRepository.deleteAll(toDelete);
            existingToDo.setToDoCurriculumList(null);
        }
    }

    public void updateStatus(Long todoId, UpdateStatusPayload payload) {
        ToDo todo = toDoRepository.findById(todoId).orElseThrow();
        todo.setStatus(payload.status());
        toDoRepository.save(todo);
    }

    public ToDo getToDo(Long todoId) {
        return toDoRepository.findById(todoId).orElseThrow();
    }

    public void deleteTodo(Long todoId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = userService.findUserByUsername(authentication.getName()).getId();

        ToDo compost = toDoRepository.findById(todoId).orElseThrow();

        if (compost.getProject() == null) {
            toDoRepository.deleteById(todoId);
        } else {
            Project preeeeejekt = compost.getProject();
            preeeeejekt.getToDos().remove(compost);

            List<Long>curryIds = new ArrayList<>();
            List<Long>userIds = new ArrayList<>();
            userIds.add(userId);
            UpdateToDoPayload payload = new UpdateToDoPayload(-1L, compost.getTitle(), compost.getDescription(), compost.getEndDate(),compost.getStartDate(), compost.getStatus(),compost.getProject().getId(),curryIds,userIds);
            assignToCurriculum(compost, payload);

            compost.setProject(null);


            toDoRepository.delete(compost);


        }


    }
}
