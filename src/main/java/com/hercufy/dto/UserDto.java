package com.hercufy.dto;

import com.hercufy.validators.annotations.SecurePassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Usado solo para login (email + contrasena). Para registro, ver RegisterRequest. */
@Data
public class UserDto {
    @NotBlank(message = "The email is required")
    @Size(min = 8, max = 40, message = "Email must be between 8 and 40 characters")
    @Email
    private String email;

    @NotBlank(message = "The password is required")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    @SecurePassword
    private String password;
}
