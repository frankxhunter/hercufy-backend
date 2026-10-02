package com.hercufy.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddPlanExerciseRequest {
    @NotBlank(message = "The exerciseId is required")
    private String exerciseId;

    @Min(value = 1, message = "sets must be at least 1")
    @Max(value = 20, message = "sets must be 20 or less")
    private int sets;

    @Min(value = 1, message = "reps must be at least 1")
    @Max(value = 200, message = "reps must be 200 or less")
    private int reps;

    // null = ejercicio sin peso.
    @DecimalMin(value = "0.0", message = "weightKg cannot be negative")
    private BigDecimal weightKg;
}
