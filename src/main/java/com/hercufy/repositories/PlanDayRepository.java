package com.hercufy.repositories;

import com.hercufy.models.PlanDay;
import com.hercufy.models.TrainingPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlanDayRepository extends JpaRepository<PlanDay, UUID> {
    Optional<PlanDay> findByIdAndPlan(UUID id, TrainingPlan plan);
}
