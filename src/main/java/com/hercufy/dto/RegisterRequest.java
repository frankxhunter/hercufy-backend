package com.hercufy.dto;

import com.hercufy.validators.annotations.SecurePassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "The username is required")
    @Size(min = 2, max = 40, message = "Username must be between 2 and 40 characters")
    private String username;

    @NotBlank(message = "The email is required")
    @Size(min = 8, max = 40, message = "Email must be between 8 and 40 characters")
    @Email
    private String email;

    @NotBlank(message = "The password is required")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    @SecurePassword
    private String password;
}
