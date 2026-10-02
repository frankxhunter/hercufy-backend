package com.hercufy.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Un ejercicio dentro de un dia de la rutina, con los datos del usuario (series,
 * repeticiones y peso). weightKg es nullable: hay ejercicios sin peso (por ejemplo,
 * los de peso corporal).
 */
@Data
@Entity
@Table(name = "plan_exercises")
public class PlanExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_day_id")
    @JsonIgnore
    private PlanDay planDay;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    private Exercise exercise;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false)
    private int sets;

    @Column(nullable = false)
    private int reps;

    @Column(precision = 6, scale = 2)
    private BigDecimal weightKg;
}
