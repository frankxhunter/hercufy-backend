package com.hercufy.services;

import com.hercufy.catalog.MuscleGroup;
import com.hercufy.catalog.TextSearch;
import com.hercufy.dto.ExerciseDetailResponse;
import com.hercufy.dto.ExerciseSummaryResponse;
import com.hercufy.dto.MuscleGroupResponse;
import com.hercufy.exceptions.ResourceNotFoundException;
import com.hercufy.models.Exercise;
import com.hercufy.models.ExerciseTranslation;
import com.hercufy.repositories.ExerciseRepository;
import com.hercufy.repositories.ExerciseTranslationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * El catalogo (unos 900 ejercicios, ver ExerciseCatalogImporter) se lee entero UNA VEZ
 * y se guarda en memoria aqui; las busquedas filtran sobre esa lista en vez de golpear
 * la base de datos en cada peticion. Es deliberadamente igual al enfoque que ya usaba
 * el prototipo del frontend (mismo normalize/tokens de TextSearch), asi el resultado
 * de una busqueda no cambia al dejar de usar datos simulados.
 *
 * Limite conocido: si algun dia se edita el catalogo en caliente (por ejemplo desde un
 * panel de administracion), hara falta llamar a refreshCache() o anadir un endpoint
 * para forzarlo; hoy la cache solo se construye al arrancar.
 */
@Service
public class ExerciseCatalogService {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseTranslationRepository translationRepository;
    private final String imagesBaseUrl;

    private volatile List<ExerciseView> cache = List.of();
    private volatile Map<String, ExerciseView> cacheById = Map.of();

    public ExerciseCatalogService(
            ExerciseRepository exerciseRepository,
            ExerciseTranslationRepository translationRepository,
            @Value("${application.exercises.images-base-url}") String imagesBaseUrl
    ) {
        this.exerciseRepository = exerciseRepository;
        this.translationRepository = translationRepository;
        this.imagesBaseUrl = imagesBaseUrl.endsWith("/") ? imagesBaseUrl : imagesBaseUrl + "/";
    }

    public synchronized void refreshCache() {
        Map<String, ExerciseTranslation> esByExerciseId = translationRepository.findByLocale("es").stream()
                .collect(Collectors.toMap(t -> t.getExercise().getId(), t -> t, (a, b) -> a));

        List<ExerciseView> views = new ArrayList<>();
        for (Exercise e : exerciseRepository.findAll()) {
            ExerciseTranslation t = esByExerciseId.get(e.getId());
            views.add(ExerciseView.from(e, t, imagesBaseUrl));
        }
        views.sort((a, b) -> a.nameEs().compareToIgnoreCase(b.nameEs()));

        this.cache = List.copyOf(views);
        this.cacheById = cache.stream().collect(Collectors.toMap(ExerciseView::id, v -> v));
    }

    public List<MuscleGroupResponse> muscleGroups() {
        return List.of(MuscleGroup.values()).stream()
                .map(g -> new MuscleGroupResponse(g.key(), g.label()))
                .toList();
    }

    public Page<ExerciseSummaryResponse> search(String q, String muscleGroupKey, Pageable pageable) {
        List<String> tokens = q == null || q.isBlank() ? List.of() : TextSearch.tokens(q);
        MuscleGroup group = muscleGroupKey == null || muscleGroupKey.isBlank() ? null : MuscleGroup.byKey(muscleGroupKey);

        List<ExerciseView> filtered = cache.stream()
                .filter(v -> group == null || group.key().equals(v.muscleGroupKey()))
                .filter(v -> tokens.isEmpty() || TextSearch.matchesAllTokens(v.haystack(), tokens))
                .toList();

        int from = Math.min((int) pageable.getOffset(), filtered.size());
        int to = Math.min(from + pageable.getPageSize(), filtered.size());
        List<ExerciseSummaryResponse> pageContent = filtered.subList(from, to).stream()
                .map(ExerciseView::toSummary)
                .toList();

        return new PageImpl<>(pageContent, pageable, filtered.size());
    }

    public ExerciseDetailResponse getDetail(String id) {
        ExerciseView view = cacheById.get(id);
        if (view == null) {
            throw new ResourceNotFoundException("Exercise not found: " + id);
        }
        return view.toDetail();
    }

    /** Usado por PlanService al validar que un ejercicio referenciado existe en el catalogo. */
    public boolean exists(String id) {
        return cacheById.containsKey(id);
    }

    public String nameEsOrFallback(String exerciseId) {
        ExerciseView v = cacheById.get(exerciseId);
        return v != null ? v.nameEs() : exerciseId;
    }

    private record ExerciseView(
            String id,
            String name,
            String nameEs,
            boolean translated,
            String level,
            String force,
            String mechanic,
            String equipment,
            String category,
            List<String> primaryMuscles,
            List<String> secondaryMuscles,
            List<String> instructions,
            List<String> imageUrls,
            String muscleGroupKey,
            String haystack
    ) {
        static ExerciseView from(Exercise e, ExerciseTranslation t, String imagesBaseUrl) {
            boolean translated = t != null && t.getName() != null && !t.getName().isBlank();
            String nameEs = translated ? t.getName() : e.getName();
            String primaryMuscle = e.getPrimaryMuscles().isEmpty() ? null : e.getPrimaryMuscles().get(0);
            MuscleGroup group = MuscleGroup.ofMuscle(primaryMuscle);
            List<String> imageUrls = e.getImages().stream().map(path -> imagesBaseUrl + path).toList();
            String haystack = TextSearch.normalize(nameEs + " " + e.getName() + " " + (e.getEquipment() == null ? "" : e.getEquipment()));

            return new ExerciseView(
                    e.getId(), e.getName(), nameEs, translated,
                    e.getLevel(), e.getForce(), e.getMechanic(), e.getEquipment(), e.getCategory(),
                    e.getPrimaryMuscles(), e.getSecondaryMuscles(), e.getInstructions(), imageUrls,
                    group != null ? group.key() : null, haystack
            );
        }

        ExerciseSummaryResponse toSummary() {
            String primaryMuscle = primaryMuscles.isEmpty() ? null : primaryMuscles.get(0);
            return new ExerciseSummaryResponse(id, name, nameEs, translated, level, equipment, category, primaryMuscle, muscleGroupKey, imageUrls);
        }

        ExerciseDetailResponse toDetail() {
            return new ExerciseDetailResponse(id, name, nameEs, translated, level, force, mechanic, equipment, category,
                    primaryMuscles, secondaryMuscles, instructions, imageUrls);
        }
    }
}
