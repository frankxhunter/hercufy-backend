package com.hercufy.controllers;

import com.hercufy.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@Validated
@RequestMapping("/api/plans")
public interface PlanController {

    @GetMapping
    ResponseEntity<?> list(Principal principal);

    @PostMapping
    ResponseEntity<?> create(Principal principal, @RequestBody @Valid CreatePlanRequest request);

    @GetMapping("/{planId}")
    ResponseEntity<?> get(Principal principal, @PathVariable UUID planId);

    @PatchMapping("/{planId}")
    ResponseEntity<?> update(Principal principal, @PathVariable UUID planId, @RequestBody @Valid UpdatePlanRequest request);

    @DeleteMapping("/{planId}")
    ResponseEntity<?> delete(Principal principal, @PathVariable UUID planId);

    /**
     * El dia de la rutina activa para el dia de la semana indicado. El "dia de hoy" lo decide
     * el cliente (dayOfWeek, 1=lunes..7=domingo) para no depender de la zona horaria del
     * servidor.
     */
    @GetMapping("/active/day")
    ResponseEntity<?> getActiveDay(Principal principal,
                                  @RequestParam("dayOfWeek") @Min(1) @Max(7) int dayOfWeek);

    @PostMapping("/{planId}/days")
    ResponseEntity<?> addDay(Principal principal, @PathVariable UUID planId, @RequestBody @Valid CreateDayRequest request);

    @PatchMapping("/{planId}/days/{dayId}")
    ResponseEntity<?> renameDay(Principal principal, @PathVariable UUID planId, @PathVariable UUID dayId,
                              @RequestBody @Valid UpdateDayRequest request);

    @DeleteMapping("/{planId}/days/{dayId}")
    ResponseEntity<?> removeDay(Principal principal, @PathVariable UUID planId, @PathVariable UUID dayId);

    @PostMapping("/{planId}/days/{dayId}/exercises")
    ResponseEntity<?> addExercise(Principal principal, @PathVariable UUID planId, @PathVariable UUID dayId,
                                 @RequestBody @Valid AddPlanExerciseRequest request);

    @PutMapping("/{planId}/days/{dayId}/exercises/{exerciseEntryId}")
    ResponseEntity<?> updateExercise(Principal principal, @PathVariable UUID planId, @PathVariable UUID dayId,
                                    @PathVariable UUID exerciseEntryId, @RequestBody @Valid UpdatePlanExerciseRequest request);

    @DeleteMapping("/{planId}/days/{dayId}/exercises/{exerciseEntryId}")
    ResponseEntity<?> removeExercise(Principal principal, @PathVariable UUID planId, @PathVariable UUID dayId,
                                    @PathVariable UUID exerciseEntryId);

    @PutMapping("/{planId}/days/{dayId}/exercises/reorder")
    ResponseEntity<?> reorderExercises(Principal principal, @PathVariable UUID planId, @PathVariable UUID dayId,
                                      @RequestBody @Valid ReorderExercisesRequest request);
}
