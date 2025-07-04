package de.dreamteam.todolist.entity;

import de.dreamteam.todolist.model.ToDoStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "todo")
public class ToDo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "creator")
    private String creator;

    @Column(name = "description")
    private String description;

    @Column(name = "endDate")
    private LocalDate endDate;

    @Column(name = "startDate")
    private LocalDate startDate;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ToDoStatus status;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @OneToMany(mappedBy = "toDo")
    private List<ToDoCurriculum> toDoCurriculumList = new ArrayList<>();

    @ManyToMany
    private List<User> userList = new ArrayList<>();
}