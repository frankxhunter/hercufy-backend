package com.hercufy.controllers.impl;

import com.hercufy.controllers.UserController;
import com.hercufy.dto.UpdateUsernameRequest;
import com.hercufy.dto.UserResponse;
import com.hercufy.models.User;
import com.hercufy.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class UserControllerImpl implements UserController {

    @Autowired
    private UserService userService;

    @Override
    public ResponseEntity<?> me(Principal principal) {
        User user = currentUser(principal);
        return ResponseEntity.ok(toResponse(user));
    }

    @Override
    public ResponseEntity<?> updateMe(Principal principal, UpdateUsernameRequest request) {
        User user = currentUser(principal);
        user = userService.updateUsername(user, request.getUsername().trim());
        return ResponseEntity.ok(toResponse(user));
    }

    @Override
    public ResponseEntity<?> deleteMe(Principal principal) {
        User user = currentUser(principal);
        userService.deleteAccount(user);
        return ResponseEntity.noContent().build();
    }

    private User currentUser(Principal principal) {
        return userService.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(),
                user.getRole() != null ? user.getRole().name() : null, user.isEmailVerified());
    }
}
