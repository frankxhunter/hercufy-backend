package com.hercufy.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Un dia dentro de una rutina (por ejemplo "Pecho, hombro y triceps", lunes).
 * dayOfWeek es 1 (lunes) a 7 (domingo); se deja nullable para poder introducir
 * mas adelante una programacion por rotacion (A/B/C) sin migrar esta tabla.
 */
@Data
@Entity
@Table(name = "plan_days")
public class PlanDay {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    @JsonIgnore
    private TrainingPlan plan;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int position;

    private Integer dayOfWeek;

    @OneToMany(mappedBy = "planDay", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<PlanExercise> exercises = new ArrayList<>();
}
