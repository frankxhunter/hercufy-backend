package com.hercufy.dto;

public record UserResponse(
        long id,
        String username,
        String email,
        String role,
        boolean emailVerified
) {
}
