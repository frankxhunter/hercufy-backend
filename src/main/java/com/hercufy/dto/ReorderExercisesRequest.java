package com.hercufy.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/** Los ids de los ejercicios del dia, en el nuevo orden deseado (deben ser todos los del dia). */
@Data
public class ReorderExercisesRequest {
    @NotEmpty(message = "orderedExerciseIds is required")
    private List<UUID> orderedExerciseIds;
}
