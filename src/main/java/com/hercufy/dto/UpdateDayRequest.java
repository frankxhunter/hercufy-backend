package com.hercufy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateDayRequest {
    @NotBlank(message = "The day name is required")
    private String name;
}
