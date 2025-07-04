package de.dreamteam.todolist.service;

import de.dreamteam.todolist.controller.payload.NewProjectPayload;
import de.dreamteam.todolist.controller.payload.UpdateProjectPayload;
import de.dreamteam.todolist.entity.Project;
import de.dreamteam.todolist.entity.User;
import de.dreamteam.todolist.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserService userService;

    // Ein neues Projekt erstellen
    /** Создать проект и сразу добавить в него текущего юзера */
    public Project createProject(NewProjectPayload newPayload) {
        // достаём текущего залогиненного
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User current = userService.findUserByUsername(username);

        Project project = Project.builder()
                .title(newPayload.title())
                .description(newPayload.description())
                .owner(current)
                .build();

        // добавляем в набор участников
        project.getUsers().add(current);
        return projectRepository.save(project);
    }

    /** Возвращает только проекты, в которых участвует текущий пользователь */
    public List<Project> getAllProjectsForCurrentUser() {
        String username = SecurityContextHolder
                .getContext().getAuthentication().getName();
        User current = userService.findUserByUsername(username);
        // вернёт все проекты, где user и/или owner == current
        return projectRepository.findDistinctByOwnerOrUsersContains(current, current);
    }


    // Projekt nach ID abrufen
    public Optional<Project> getProjectById(Long id) {
        return projectRepository.findById(id);
    }

    public Project getProjectByIdForCurrentUser(Long id) throws AccessDeniedException {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User current = userService.findUserByUsername(username);

        boolean isOwner = project.getOwner().getId().equals(current.getId());
        boolean isMember = project.getUsers().contains(current);

        if (!isOwner && !isMember) {
            throw new AccessDeniedException("Access denied");
        }
        return project;
    }

    // Projektdaten nach ID aktualisieren
    public Optional<Project> updateProject(Long id, UpdateProjectPayload updatePayload) {
        return projectRepository.findById(id).map(existingProject -> {
            // Hier findet die Zuordnung von DTO zu Entity statt:
            existingProject.setTitle(updatePayload.title());
            existingProject.setDescription(updatePayload.description());
            return projectRepository.save(existingProject);
        });
    }

    public Project updateProjectForCurrentUser(Long id, UpdateProjectPayload payload) throws AccessDeniedException {
        // бросит 404 или 403 при отсутствии проекта или правах
        Project project = getProjectByIdForCurrentUser(id);
        project.setTitle(payload.title());
        project.setDescription(payload.description());
        return projectRepository.save(project);
    }

    // Projekt nach ID löschen
    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    public void deleteProjectForCurrentUser(Long id) throws AccessDeniedException {
        Project project = getProjectByIdForCurrentUser(id);
        projectRepository.delete(project);
    }

    /** Пригласить пользователя в проект */
    public void inviteUser(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
        User toInvite = userService.getUserById(userId);
        if (toInvite == null) {
            throw new EntityNotFoundException("User not found");
        }
        project.getUsers().add(toInvite);
        projectRepository.save(project);
    }
}

