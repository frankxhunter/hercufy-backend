package com.hercufy.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/** Reemplaza series, repeticiones y peso a la vez (asi lo edita siempre la pantalla del dia). */
@Data
public class UpdatePlanExerciseRequest {
    @Min(value = 1, message = "sets must be at least 1")
    @Max(value = 20, message = "sets must be 20 or less")
    private int sets;

    @Min(value = 1, message = "reps must be at least 1")
    @Max(value = 200, message = "reps must be 200 or less")
    private int reps;

    @DecimalMin(value = "0.0", message = "weightKg cannot be negative")
    private BigDecimal weightKg;
}
