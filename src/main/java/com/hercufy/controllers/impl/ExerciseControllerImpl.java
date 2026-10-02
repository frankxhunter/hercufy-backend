package com.hercufy.controllers.impl;

import com.hercufy.controllers.ExerciseController;
import com.hercufy.services.ExerciseCatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExerciseControllerImpl implements ExerciseController {

    @Autowired
    private ExerciseCatalogService catalogService;

    @Override
    public ResponseEntity<?> search(String q, String muscleGroup, Pageable pageable) {
        return ResponseEntity.ok(catalogService.search(q, muscleGroup, pageable));
    }

    @Override
    public ResponseEntity<?> muscleGroups() {
        return ResponseEntity.ok(catalogService.muscleGroups());
    }

    @Override
    public ResponseEntity<?> getById(String id) {
        return ResponseEntity.ok(catalogService.getDetail(id));
    }
}
