package com.hercufy.dto;

import java.util.List;

/** Version resumida de un ejercicio, para listados y busqueda. */
public record ExerciseSummaryResponse(
        String id,
        String name,
        String nameEs,
        boolean translated,
        String level,
        String equipment,
        String category,
        String primaryMuscle,
        String muscleGroup,
        List<String> imageUrls
) {
}
