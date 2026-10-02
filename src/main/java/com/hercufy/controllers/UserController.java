package com.hercufy.controllers;

import com.hercufy.dto.UpdateUsernameRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RequestMapping("/api/users")
public interface UserController {

    @GetMapping("/me")
    ResponseEntity<?> me(Principal principal);

    @PatchMapping("/me")
    ResponseEntity<?> updateMe(Principal principal, @RequestBody @Valid UpdateUsernameRequest request);

    /**
     * Borrado de cuenta. Requisito de las tiendas de apps (Apple/Google): tiene que
     * poder hacerse desde dentro de la propia aplicacion.
     */
    @DeleteMapping("/me")
    ResponseEntity<?> deleteMe(Principal principal);
}
