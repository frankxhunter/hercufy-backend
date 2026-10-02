package com.hercufy.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Traduccion de un ejercicio del catalogo a un idioma dado. Empieza vacia salvo por un
 * primer lote en espanol (ver ExerciseTranslationImporter); anadir un idioma no toca
 * la tabla "exercises". Si no hay traduccion para un ejercicio, la API devuelve el
 * nombre y las instrucciones originales en ingles como alternativa.
 */
@Data
@Entity
@Table(name = "exercise_translations", uniqueConstraints = @UniqueConstraint(columnNames = {"exercise_id", "locale"}))
public class ExerciseTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    private Exercise exercise;

    // Codigo de idioma, por ejemplo "es".
    @Column(nullable = false, length = 8)
    private String locale;

    @Column(nullable = false)
    private String name;

    // Puede quedar vacia si solo se ha traducido el nombre por ahora.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "exercise_translation_instructions", joinColumns = @JoinColumn(name = "translation_id"))
    @OrderColumn(name = "step_order")
    @Column(name = "instruction", columnDefinition = "TEXT", nullable = false)
    private List<String> instructions = new ArrayList<>();
}
