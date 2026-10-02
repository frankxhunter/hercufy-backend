package com.hercufy.dto;

import java.util.List;

/** Version completa de un ejercicio, para la pantalla de detalle. */
public record ExerciseDetailResponse(
        String id,
        String name,
        String nameEs,
        boolean translated,
        String level,
        String force,
        String mechanic,
        String equipment,
        String category,
        List<String> primaryMuscles,
        List<String> secondaryMuscles,
        List<String> instructions,
        List<String> imageUrls
) {
}
