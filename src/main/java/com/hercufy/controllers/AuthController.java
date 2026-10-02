package com.hercufy.controllers;

import com.hercufy.dto.GoogleToken;
import com.hercufy.dto.RefreshTokenRequest;
import com.hercufy.dto.RegisterRequest;
import com.hercufy.dto.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Validated
@RequestMapping("/api/auth")
public interface AuthController {

    @PostMapping("/register")
    ResponseEntity<?> register(
            @RequestBody @Valid RegisterRequest registerRequest,
            HttpServletRequest request
    );

    @PostMapping("/login")
    ResponseEntity<?> logIn(
            @RequestBody @Valid UserDto userDto,
            HttpServletRequest request
    );

    @GetMapping("/login")
    ResponseEntity<?> checkLogin(Principal principal);

    @PostMapping("/google")
    ResponseEntity<?> googleLogin(
            @Valid
            @RequestBody
            GoogleToken token,
            HttpServletRequest request
    ) throws Exception;

    @PostMapping("/refresh")
    ResponseEntity<?> refreshToken(@RequestBody @Valid RefreshTokenRequest refreshTokenRequest);

    @GetMapping("/confirm-email")
    ResponseEntity<?> confirmEmail(@RequestParam("token") @NotBlank(message = "Token is required") String token);

    @PostMapping("/logout")
    ResponseEntity<?> logout(
            HttpServletRequest request,
            @RequestBody @Valid RefreshTokenRequest refreshTokenRequest
    );
}
