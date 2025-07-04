package de.dreamteam.todolist.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "curriculum")
public class  Curriculum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // Name der Aufgabe

    // Verbindung mit dem Benutzer. Ein Lehrplan kann ein Benutzer enthalten
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Eins-zu-Viele-Beziehung zur assoziativen Entität (Aufgabe - Lehrplan)
    @OneToMany(mappedBy = "curriculum", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ToDoCurriculum> toDoCurriculumList = new ArrayList<>();

}
