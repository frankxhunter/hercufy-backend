package com.hercufy.controllers.impl;

import com.hercufy.controllers.PlanController;
import com.hercufy.dto.*;
import com.hercufy.models.User;
import com.hercufy.services.PlanService;
import com.hercufy.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.UUID;

@RestController
public class PlanControllerImpl implements PlanController {

    @Autowired
    private PlanService planService;

    @Autowired
    private UserService userService;

    @Override
    public ResponseEntity<?> list(Principal principal) {
        return ResponseEntity.ok(planService.listPlans(user(principal)));
    }

    @Override
    public ResponseEntity<?> create(Principal principal, CreatePlanRequest request) {
        return ResponseEntity.status(201).body(planService.createPlan(user(principal), request));
    }

    @Override
    public ResponseEntity<?> get(Principal principal, UUID planId) {
        return ResponseEntity.ok(planService.getPlan(user(principal), planId));
    }

    @Override
    public ResponseEntity<?> update(Principal principal, UUID planId, UpdatePlanRequest request) {
        return ResponseEntity.ok(planService.updatePlan(user(principal), planId, request));
    }

    @Override
    public ResponseEntity<?> delete(Principal principal, UUID planId) {
        planService.deletePlan(user(principal), planId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<?> getActiveDay(Principal principal, int dayOfWeek) {
        return ResponseEntity.ok(planService.getActivePlanDay(user(principal), dayOfWeek));
    }

    @Override
    public ResponseEntity<?> addDay(Principal principal, UUID planId, CreateDayRequest request) {
        return ResponseEntity.status(201).body(planService.addDay(user(principal), planId, request));
    }

    @Override
    public ResponseEntity<?> renameDay(Principal principal, UUID planId, UUID dayId, UpdateDayRequest request) {
        return ResponseEntity.ok(planService.renameDay(user(principal), planId, dayId, request));
    }

    @Override
    public ResponseEntity<?> removeDay(Principal principal, UUID planId, UUID dayId) {
        return ResponseEntity.ok(planService.removeDay(user(principal), planId, dayId));
    }

    @Override
    public ResponseEntity<?> addExercise(Principal principal, UUID planId, UUID dayId, AddPlanExerciseRequest request) {
        return ResponseEntity.status(201).body(planService.addExercise(user(principal), planId, dayId, request));
    }

    @Override
    public ResponseEntity<?> updateExercise(Principal principal, UUID planId, UUID dayId, UUID exerciseEntryId,
                                           UpdatePlanExerciseRequest request) {
        return ResponseEntity.ok(planService.updateExercise(user(principal), planId, dayId, exerciseEntryId, request));
    }

    @Override
    public ResponseEntity<?> removeExercise(Principal principal, UUID planId, UUID dayId, UUID exerciseEntryId) {
        return ResponseEntity.ok(planService.removeExercise(user(principal), planId, dayId, exerciseEntryId));
    }

    @Override
    public ResponseEntity<?> reorderExercises(Principal principal, UUID planId, UUID dayId, ReorderExercisesRequest request) {
        return ResponseEntity.ok(planService.reorderExercises(user(principal), planId, dayId, request));
    }

    private User user(Principal principal) {
        return userService.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
