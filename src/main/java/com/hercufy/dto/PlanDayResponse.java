package com.hercufy.dto;

import java.util.List;
import java.util.UUID;

public record PlanDayResponse(
        UUID id,
        String name,
        int position,
        Integer dayOfWeek,
        List<PlanExerciseResponse> exercises
) {
}
