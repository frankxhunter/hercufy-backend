package com.hercufy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUsernameRequest {
    @NotBlank(message = "The username is required")
    @Size(min = 2, max = 40, message = "Username must be between 2 and 40 characters")
    private String username;
}
