package com.hercufy.services;

import com.hercufy.dto.*;
import com.hercufy.exceptions.ResourceNotFoundException;
import com.hercufy.models.*;
import com.hercufy.repositories.ExerciseRepository;
import com.hercufy.repositories.PlanDayRepository;
import com.hercufy.repositories.PlanExerciseRepository;
import com.hercufy.repositories.TrainingPlanRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Toda la logica de rutinas pasa por aqui, tanto si el origen es la edicion manual como,
 * mas adelante, la aceptacion de un borrador de Hercules: un unico camino evita que las
 * dos vias diverjan (ver plan tecnico, seccion 5).
 *
 * Todas las operaciones reciben el User autenticado y comprueban que el recurso le
 * pertenece con findByIdAndUser / findByIdAndPlan / findByIdAndPlanDay; si no es asi,
 * se lanza ResourceNotFoundException (404) en vez de un 403, para no revelar si el
 * recurso existe.
 */
@Service
public class PlanService {

    private final TrainingPlanRepository planRepository;
    private final PlanDayRepository dayRepository;
    private final PlanExerciseRepository exerciseRepository;
    private final ExerciseRepository catalogRepository;
    private final ExerciseCatalogService catalogService;

    public PlanService(
            TrainingPlanRepository planRepository,
            PlanDayRepository dayRepository,
            PlanExerciseRepository exerciseRepository,
            ExerciseRepository catalogRepository,
            ExerciseCatalogService catalogService
    ) {
        this.planRepository = planRepository;
        this.dayRepository = dayRepository;
        this.exerciseRepository = exerciseRepository;
        this.catalogRepository = catalogRepository;
        this.catalogService = catalogService;
    }

    // ---- Rutinas ----

    @Transactional(readOnly = true)
    public List<TrainingPlanResponse> listPlans(User user) {
        return planRepository.findByUserOrderByCreatedAtAsc(user).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TrainingPlanResponse getPlan(User user, UUID planId) {
        return toResponse(findOwnedPlan(user, planId));
    }

    /** El dia de la rutina activa que corresponde al dia de la semana indicado (1 a 7), si hay alguno. */
    @Transactional(readOnly = true)
    public PlanDayResponse getActivePlanDay(User user, int dayOfWeek) {
        List<TrainingPlan> active = planRepository.findByUserAndActiveTrue(user);
        if (active.isEmpty()) {
            throw new ResourceNotFoundException("No hay ninguna rutina activa");
        }
        return active.get(0).getDays().stream()
                .filter(d -> d.getDayOfWeek() != null && d.getDayOfWeek() == dayOfWeek)
                .findFirst()
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("La rutina activa no tiene entrenamiento ese dia"));
    }

    @Transactional
    public TrainingPlanResponse createPlan(User user, CreatePlanRequest request) {
        TrainingPlan plan = new TrainingPlan();
        plan.setUser(user);
        plan.setName(request.getName().trim());
        plan.setScheduleType(ScheduleType.WEEKDAY);
        // La primera rutina del usuario se activa sola; las siguientes se crean inactivas.
        plan.setActive(planRepository.findByUserOrderByCreatedAtAsc(user).isEmpty());

        int position = 0;
        for (CreatePlanDayItem item : request.getDays()) {
            PlanDay day = new PlanDay();
            day.setPlan(plan);
            day.setName(item.getName().trim());
            day.setDayOfWeek(item.getDayOfWeek());
            day.setPosition(position++);
            plan.getDays().add(day);
        }

        return toResponse(planRepository.save(plan));
    }

    @Transactional
    public TrainingPlanResponse updatePlan(User user, UUID planId, UpdatePlanRequest request) {
        TrainingPlan plan = findOwnedPlan(user, planId);

        if (request.getName() != null) {
            plan.setName(request.getName().trim());
        }
        if (request.getActive() != null) {
            applyActive(user, plan, request.getActive());
        }

        return toResponse(planRepository.save(plan));
    }

    @Transactional
    public void deletePlan(User user, UUID planId) {
        TrainingPlan plan = findOwnedPlan(user, planId);
        planRepository.delete(plan);
    }

    private void applyActive(User user, TrainingPlan plan, boolean active) {
        if (active) {
            // Solo una rutina activa por usuario: se desactivan las demas.
            for (TrainingPlan other : planRepository.findByUserAndActiveTrue(user)) {
                if (!other.getId().equals(plan.getId())) {
                    other.setActive(false);
                    planRepository.save(other);
                }
            }
        }
        plan.setActive(active);
    }

    // ---- Dias ----

    @Transactional
    public TrainingPlanResponse addDay(User user, UUID planId, CreateDayRequest request) {
        TrainingPlan plan = findOwnedPlan(user, planId);

        PlanDay day = new PlanDay();
        day.setPlan(plan);
        day.setName(request.getName().trim());
        day.setDayOfWeek(request.getDayOfWeek());
        day.setPosition(plan.getDays().size());
        plan.getDays().add(day);

        return toResponse(planRepository.save(plan));
    }

    @Transactional
    public TrainingPlanResponse renameDay(User user, UUID planId, UUID dayId, UpdateDayRequest request) {
        TrainingPlan plan = findOwnedPlan(user, planId);
        PlanDay day = findDay(plan, dayId);
        day.setName(request.getName().trim());
        dayRepository.save(day);
        return toResponse(plan);
    }

    @Transactional
    public TrainingPlanResponse removeDay(User user, UUID planId, UUID dayId) {
        TrainingPlan plan = findOwnedPlan(user, planId);
        PlanDay day = findDay(plan, dayId);
        plan.getDays().remove(day);
        return toResponse(planRepository.save(plan));
    }

    // ---- Ejercicios de un dia ----

    @Transactional
    public TrainingPlanResponse addExercise(User user, UUID planId, UUID dayId, AddPlanExerciseRequest request) {
        TrainingPlan plan = findOwnedPlan(user, planId);
        PlanDay day = findDay(plan, dayId);
        Exercise exercise = findCatalogExercise(request.getExerciseId());

        PlanExercise pe = new PlanExercise();
        pe.setPlanDay(day);
        pe.setExercise(exercise);
        pe.setSets(request.getSets());
        pe.setReps(request.getReps());
        pe.setWeightKg(request.getWeightKg());
        pe.setPosition(day.getExercises().size());
        day.getExercises().add(pe);

        return toResponse(planRepository.save(plan));
    }

    @Transactional
    public TrainingPlanResponse updateExercise(User user, UUID planId, UUID dayId, UUID exerciseEntryId,
                                              UpdatePlanExerciseRequest request) {
        TrainingPlan plan = findOwnedPlan(user, planId);
        PlanDay day = findDay(plan, dayId);
        PlanExercise pe = findPlanExercise(day, exerciseEntryId);

        pe.setSets(request.getSets());
        pe.setReps(request.getReps());
        pe.setWeightKg(request.getWeightKg());

        return toResponse(planRepository.save(plan));
    }

    @Transactional
    public TrainingPlanResponse removeExercise(User user, UUID planId, UUID dayId, UUID exerciseEntryId) {
        TrainingPlan plan = findOwnedPlan(user, planId);
        PlanDay day = findDay(plan, dayId);
        PlanExercise pe = findPlanExercise(day, exerciseEntryId);
        day.getExercises().remove(pe);
        return toResponse(planRepository.save(plan));
    }

    @Transactional
    public TrainingPlanResponse reorderExercises(User user, UUID planId, UUID dayId, ReorderExercisesRequest request) {
        TrainingPlan plan = findOwnedPlan(user, planId);
        PlanDay day = findDay(plan, dayId);

        List<UUID> wanted = request.getOrderedExerciseIds();
        if (wanted.size() != day.getExercises().size()
                || !wanted.containsAll(day.getExercises().stream().map(PlanExercise::getId).toList())) {
            throw new IllegalArgumentException("orderedExerciseIds must contain exactly the day's current exercises");
        }

        for (PlanExercise pe : day.getExercises()) {
            pe.setPosition(wanted.indexOf(pe.getId()));
        }
        day.getExercises().sort((a, b) -> Integer.compare(a.getPosition(), b.getPosition()));

        return toResponse(planRepository.save(plan));
    }

    // ---- Helpers ----

    private TrainingPlan findOwnedPlan(User user, UUID planId) {
        return planRepository.findByIdAndUser(planId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + planId));
    }

    private PlanDay findDay(TrainingPlan plan, UUID dayId) {
        return dayRepository.findByIdAndPlan(dayId, plan)
                .orElseThrow(() -> new ResourceNotFoundException("Day not found: " + dayId));
    }

    private PlanExercise findPlanExercise(PlanDay day, UUID exerciseEntryId) {
        return exerciseRepository.findByIdAndPlanDay(exerciseEntryId, day)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise entry not found: " + exerciseEntryId));
    }

    private Exercise findCatalogExercise(String exerciseId) {
        if (!catalogService.exists(exerciseId)) {
            throw new ResourceNotFoundException("Exercise not found in catalog: " + exerciseId);
        }
        return catalogRepository.findById(exerciseId)
                .orElseThrow(() -> new EntityNotFoundException("Exercise not found in catalog: " + exerciseId));
    }

    private TrainingPlanResponse toResponse(TrainingPlan plan) {
        List<PlanDayResponse> days = plan.getDays().stream()
                .sorted((a, b) -> Integer.compare(a.getPosition(), b.getPosition()))
                .map(this::toResponse)
                .toList();
        return new TrainingPlanResponse(plan.getId(), plan.getName(), plan.isActive(),
                plan.getScheduleType().name(), days, plan.getCreatedAt(), plan.getUpdatedAt());
    }

    private PlanDayResponse toResponse(PlanDay day) {
        List<PlanExerciseResponse> exercises = day.getExercises().stream()
                .sorted((a, b) -> Integer.compare(a.getPosition(), b.getPosition()))
                .map(this::toResponse)
                .toList();
        return new PlanDayResponse(day.getId(), day.getName(), day.getPosition(), day.getDayOfWeek(), exercises);
    }

    private PlanExerciseResponse toResponse(PlanExercise pe) {
        Exercise exercise = pe.getExercise();
        return new PlanExerciseResponse(pe.getId(), exercise.getId(), exercise.getName(),
                catalogService.nameEsOrFallback(exercise.getId()), pe.getPosition(), pe.getSets(), pe.getReps(), pe.getWeightKg());
    }
}
