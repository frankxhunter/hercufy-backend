package com.hercufy.services;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/** Forma exacta de cada elemento del JSON de free-exercise-db (dist/exercises.json). */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class ExerciseImportRecord {
    private String id;
    private String name;
    private String force;
    private String level;
    private String mechanic;
    private String equipment;
    private String category;
    private List<String> primaryMuscles;
    private List<String> secondaryMuscles;
    private List<String> instructions;
    private List<String> images;
}
