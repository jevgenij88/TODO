package de.dreamteam.todolist.service;

import de.dreamteam.todolist.controller.payload.NewCurriculumPayload;
import de.dreamteam.todolist.controller.payload.UpdateCurriculumPayload;
import de.dreamteam.todolist.entity.Curriculum;
import de.dreamteam.todolist.entity.User;
import de.dreamteam.todolist.repository.CurriculumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class CurriculumService {

    private final CurriculumRepository curriculumRepository;
    private final UserService userService;


    // Erstellung eines neuen Lehrplans (Curriculum)
    public Curriculum createCurriculum(NewCurriculumPayload newPayload) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        User currentUser = userService.findUserByUsername(currentUsername);
        if (currentUser == null) {
            throw new RuntimeException("User not found");
        }
        Curriculum curriculum = Curriculum.builder()
                .title(newPayload.title())
                .user(currentUser)
                .build();
        return curriculumRepository.save(curriculum);
    }


    // Abrufen des Lehrplans nach ID
    public Optional<Curriculum> getCurriculumForCurrentUser() {
        // Извлекаем аутентифицированного пользователя
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        User currentUser = userService.findUserByUsername(currentUsername);
        if (currentUser == null) {
            return Optional.empty();
        }
        return curriculumRepository.findByUser_Id(currentUser.getId());
    }


    // Aktualisierung des Lehrplans auf id
    public Optional<Curriculum> updateCurriculumForCurrentUser(UpdateCurriculumPayload updatePayload) {
        // Извлекаем текущего пользователя
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        User currentUser = userService.findUserByUsername(currentUsername);
        if (currentUser == null) {
            return Optional.empty();
        }
        // Abrufen des Lehrplans für den aktuellen Benutzer
        Optional<Curriculum> curriculumOpt = curriculumRepository.findByUser_Id(currentUser.getId());
        return curriculumOpt.map(existingCurriculum -> {
            existingCurriculum.setTitle(updatePayload.title());

            // Überschreiben Sie das Benutzerfeld nicht - setzen Sie den aktuellen Benutzer aus Sicherheitsgründen
            existingCurriculum.setUser(currentUser);
            return curriculumRepository.save(existingCurriculum);
        });
    }

    // Löschen eines Lehrplans current user
    public void deleteCurriculumForCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        User currentUser = userService.findUserByUsername(currentUsername);
        // Предполагается, что в репозитории определён метод для поиска по user.id
        Optional<Curriculum> curriculumOpt = curriculumRepository.findByUser_Id(currentUser.getId());
        if (curriculumOpt.isEmpty()) {
            throw new RuntimeException("Curriculum not found for current user");
        }
        curriculumRepository.delete(curriculumOpt.get());
    }
}

