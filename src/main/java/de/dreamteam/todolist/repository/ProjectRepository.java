package de.dreamteam.todolist.repository;

import de.dreamteam.todolist.entity.Project;
import de.dreamteam.todolist.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findDistinctByOwnerOrUsersContains(User owner, User user);
}

