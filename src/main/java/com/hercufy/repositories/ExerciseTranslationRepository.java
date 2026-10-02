package com.hercufy.repositories;

import com.hercufy.models.ExerciseTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseTranslationRepository extends JpaRepository<ExerciseTranslation, Long> {
    List<ExerciseTranslation> findByLocale(String locale);

    long countByLocale(String locale);
}
