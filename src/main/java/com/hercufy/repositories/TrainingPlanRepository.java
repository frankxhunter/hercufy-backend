package com.hercufy.repositories;

import com.hercufy.models.TrainingPlan;
import com.hercufy.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrainingPlanRepository extends JpaRepository<TrainingPlan, UUID> {

    List<TrainingPlan> findByUserOrderByCreatedAtAsc(User user);

    // Se filtra siempre por el usuario duenio: evita que alguien acceda a la rutina de otro
    // simplemente adivinando el UUID, y a la vez permite devolver 404 en vez de 403 sin
    // filtrar si el recurso existe o no.
    Optional<TrainingPlan> findByIdAndUser(UUID id, User user);

    List<TrainingPlan> findByUserAndActiveTrue(User user);
}
