package com.hercufy.services;

import com.hercufy.dto.RegisterRequest;
import com.hercufy.models.User;
import com.hercufy.repositories.UserRepository;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private EmailVerificationService emailVerificationService;

    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public User save(User user) {
        processPassword(user);
        user = repository.save(user);
        return user;
    }

    public User update(User foundUser, RegisterRequest registerRequest) {
        foundUser.setUsername(registerRequest.getUsername());
        foundUser.setPassword(processedPassword(registerRequest.getPassword()));
        repository.save(foundUser);
        return foundUser;
    }

    public User updateUsername(User user, String username) {
        user.setUsername(username);
        return repository.save(user);
    }

    /**
     * Borra la cuenta y todo lo que dependa de ella: tokens de sesion y de
     * verificacion de email, y las rutinas del usuario (User.plans tiene
     * cascade + orphanRemoval, asi que Hibernate las borra el borrar el usuario).
     */
    @Transactional
    public void deleteAccount(User user) {
        refreshTokenService.deleteTokensForUser(user);
        emailVerificationService.deleteTokensForUser(user);
        repository.delete(user);
    }

    private void processPassword(User user) {
        user.setPassword(processedPassword(user.getPassword()));
    }

    private String processedPassword(String rawPassword) {
        if (StringUtils.isNotBlank(rawPassword)) {
            return passwordEncoder.encode(rawPassword);
        }
        return null;
    }

    public User convertFromDto(RegisterRequest registerRequest) {
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword());
        return user;
    }

    /** Nombre de usuario por defecto para cuentas creadas via Google, a partir del correo. */
    public String usernameFromEmail(String email) {
        String local = email.split("@")[0].replaceAll("[._-]+", " ").trim();
        if (local.isEmpty()) {
            return "Usuario";
        }
        return local.substring(0, 1).toUpperCase(Locale.ROOT) + local.substring(1);
    }
}
