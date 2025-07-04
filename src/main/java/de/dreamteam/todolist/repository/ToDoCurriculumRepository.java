package de.dreamteam.todolist.repository;

import de.dreamteam.todolist.entity.ToDoCurriculum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ToDoCurriculumRepository extends JpaRepository<ToDoCurriculum, Long> {
    Optional<ToDoCurriculum> findByCurriculum_IdAndToDo_Id(Long curriculumId, Long toDoId);
}

