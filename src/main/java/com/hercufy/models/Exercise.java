package com.hercufy.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Catalogo de ejercicios importado de free-exercise-db (ver ExerciseImporter).
 * El id coincide con el "id" del dataset original (por ejemplo "Barbell_Bench_Press_-_Medium_Grip").
 *
 * Las colecciones se cargan EAGER a proposito: el catalogo es pequeno (unos 900 ejercicios) y se
 * lee por completo una unica vez al arrancar para construir la cache en memoria de
 * ExerciseCatalogService. Evita el problema de "MultipleBagFetchException" que aparece al
 * intentar traer varias colecciones de golpe con fetch join, a cambio de varias consultas
 * pequenas en el arranque, que es un coste asumible al no repetirse por cada peticion.
 */
@Data
@Entity
@Table(name = "exercises")
public class Exercise {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    private String category;

    private String level;

    // Pueden venir vacios en el dataset original.
    private String force;

    private String mechanic;

    private String equipment;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "exercise_primary_muscles", joinColumns = @JoinColumn(name = "exercise_id"))
    @Column(name = "muscle", nullable = false)
    private List<String> primaryMuscles = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "exercise_secondary_muscles", joinColumns = @JoinColumn(name = "exercise_id"))
    @Column(name = "muscle", nullable = false)
    private List<String> secondaryMuscles = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "exercise_instructions", joinColumns = @JoinColumn(name = "exercise_id"))
    @OrderColumn(name = "step_order")
    @Column(name = "instruction", columnDefinition = "TEXT", nullable = false)
    private List<String> instructions = new ArrayList<>();

    // Rutas relativas dentro del repositorio free-exercise-db; se combinan con
    // application.exercises.images-base-url para formar la URL final (ver ExerciseCatalogService).
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "exercise_images", joinColumns = @JoinColumn(name = "exercise_id"))
    @OrderColumn(name = "image_order")
    @Column(name = "path", nullable = false)
    private List<String> images = new ArrayList<>();
}
