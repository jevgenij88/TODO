package de.dreamteam.todolist.repository;

import de.dreamteam.todolist.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findUserByUsername(String username);

    User findUserByEmail(String email);

    User findUserByResetToken(String token);

    User findUserByVerificationToken(String token);

    void deleteByEmail(String email);
}
