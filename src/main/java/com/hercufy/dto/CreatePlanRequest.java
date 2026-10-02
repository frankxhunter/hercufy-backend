package com.hercufy.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/** Creacion manual de una rutina: nombre y sus dias (sin ejercicios todavia). */
@Data
public class CreatePlanRequest {
    @NotBlank(message = "The plan name is required")
    private String name;

    @NotEmpty(message = "At least one training day is required")
    @Valid
    private List<CreatePlanDayItem> days;
}
