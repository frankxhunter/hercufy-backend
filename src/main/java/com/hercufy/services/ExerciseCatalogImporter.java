package com.hercufy.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hercufy.models.Exercise;
import com.hercufy.models.ExerciseTranslation;
import com.hercufy.repositories.ExerciseRepository;
import com.hercufy.repositories.ExerciseTranslationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Siembra el catalogo de ejercicios (free-exercise-db) y sus traducciones al arrancar,
 * si las tablas estan vacias. Los datos vienen empaquetados en resources/data para que
 * el backend no dependa de internet en tiempo de ejecucion.
 *
 * IMPORTANTE: verificar la licencia de free-exercise-db (github.com/yuhonas/free-exercise-db)
 * antes de publicar la aplicacion; ver la seccion de riesgos del plan tecnico.
 */
@Component
public class ExerciseCatalogImporter implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ExerciseCatalogImporter.class);
    private static final String ES_LOCALE = "es";

    private final ExerciseRepository exerciseRepository;
    private final ExerciseTranslationRepository translationRepository;
    private final ExerciseCatalogService catalogService;
    private final ObjectMapper objectMapper;

    public ExerciseCatalogImporter(
            ExerciseRepository exerciseRepository,
            ExerciseTranslationRepository translationRepository,
            ExerciseCatalogService catalogService,
            ObjectMapper objectMapper
    ) {
        this.exerciseRepository = exerciseRepository;
        this.translationRepository = translationRepository;
        this.catalogService = catalogService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        importExercisesIfEmpty();
        importTranslationsIfEmpty();
        catalogService.refreshCache();
    }

    private void importExercisesIfEmpty() throws Exception {
        if (exerciseRepository.count() > 0) {
            log.info("Catalogo de ejercicios ya existe ({} filas); se omite la importacion.", exerciseRepository.count());
            return;
        }
        List<ExerciseImportRecord> records = readJson("data/exercises.json", ExerciseImportRecord[].class);
        List<Exercise> exercises = new ArrayList<>(records.size());
        for (ExerciseImportRecord r : records) {
            Exercise e = new Exercise();
            e.setId(r.getId());
            e.setName(r.getName());
            e.setCategory(r.getCategory());
            e.setLevel(r.getLevel());
            e.setForce(r.getForce());
            e.setMechanic(r.getMechanic());
            e.setEquipment(r.getEquipment());
            e.setPrimaryMuscles(r.getPrimaryMuscles() != null ? r.getPrimaryMuscles() : List.of());
            e.setSecondaryMuscles(r.getSecondaryMuscles() != null ? r.getSecondaryMuscles() : List.of());
            e.setInstructions(r.getInstructions() != null ? r.getInstructions() : List.of());
            e.setImages(r.getImages() != null ? r.getImages() : List.of());
            exercises.add(e);
        }
        exerciseRepository.saveAll(exercises);
        log.info("Catalogo de ejercicios importado: {} ejercicios.", exercises.size());
    }

    private void importTranslationsIfEmpty() throws Exception {
        if (translationRepository.countByLocale(ES_LOCALE) > 0) {
            log.info("Traducciones al espanol ya existen; se omite la importacion.");
            return;
        }
        List<ExerciseTranslationImportRecord> records =
                readJson("data/exercise-translations-es.json", ExerciseTranslationImportRecord[].class);
        List<ExerciseTranslation> translations = new ArrayList<>(records.size());
        for (ExerciseTranslationImportRecord r : records) {
            Exercise exercise = exerciseRepository.findById(r.getId()).orElse(null);
            if (exercise == null) {
                log.warn("Traduccion ignorada: no existe el ejercicio con id {}", r.getId());
                continue;
            }
            ExerciseTranslation t = new ExerciseTranslation();
            t.setExercise(exercise);
            t.setLocale(ES_LOCALE);
            t.setName(r.getNameEs());
            translations.add(t);
        }
        translationRepository.saveAll(translations);
        log.info("Traducciones al espanol importadas: {} nombres.", translations.size());
    }

    private <T> List<T> readJson(String classpathPath, Class<T[]> arrayType) throws Exception {
        try (InputStream in = new ClassPathResource(classpathPath).getInputStream()) {
            T[] array = objectMapper.readValue(in, arrayType);
            return List.of(array);
        }
    }
}
