package com.hercufy.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true, nullable = false)
    private String email;

    @JsonIgnore
    private String password;

    // Nombre visible del usuario (distinto del email, que es el identificador de acceso).
    @Column(nullable = false)
    private String username;

    private Roles role;

    private boolean emailVerified = false;

    // cascade + orphanRemoval: al borrar la cuenta (UserService.deleteAccount), JPA borra
    // tambien sus rutinas. No se expone en el JSON del usuario (no hace falta @JsonIgnore
    // porque TrainingPlan.user ya lo lleva del otro lado).
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrainingPlan> plans = new ArrayList<>();
}
