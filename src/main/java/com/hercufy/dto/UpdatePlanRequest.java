package com.hercufy.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/** Actualizacion parcial: solo se aplican los campos que vienen distintos de null. */
@Data
public class UpdatePlanRequest {
    @Size(min = 1, message = "The plan name cannot be empty")
    private String name;

    private Boolean active;
}
