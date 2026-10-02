package com.hercufy.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateDayRequest {
    @NotBlank(message = "The day name is required")
    private String name;

    @Min(value = 1, message = "dayOfWeek must be between 1 (Monday) and 7 (Sunday)")
    @Max(value = 7, message = "dayOfWeek must be between 1 (Monday) and 7 (Sunday)")
    private int dayOfWeek;
}
