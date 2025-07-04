package de.dreamteam.todolist.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "todo_curriculum")

public class ToDoCurriculum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    // Verknüpfung von Aufgaben
    @ManyToOne(optional = false)
    @JoinColumn(name = "todo_id")
    private ToDo toDo;

    // Verknüpfung von Lernplan
    @ManyToOne(optional = false)
    @JoinColumn(name = "curriculum_id")
    private Curriculum curriculum;
}
