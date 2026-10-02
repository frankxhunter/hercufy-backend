package com.hercufy.controllers;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/exercises")
public interface ExerciseController {

    @GetMapping
    ResponseEntity<?> search(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "muscleGroup", required = false) String muscleGroup,
            @PageableDefault(size = 20) Pageable pageable
    );

    @GetMapping("/muscle-groups")
    ResponseEntity<?> muscleGroups();

    @GetMapping("/{id}")
    ResponseEntity<?> getById(@PathVariable String id);
}
