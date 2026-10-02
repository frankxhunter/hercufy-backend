package com.hercufy.dto;

import java.math.BigDecimal;

public record PlanExerciseResponse(
        java.util.UUID id,
        String exerciseId,
        String exerciseName,
        String exerciseNameEs,
        int position,
        int sets,
        int reps,
        BigDecimal weightKg
) {
}
