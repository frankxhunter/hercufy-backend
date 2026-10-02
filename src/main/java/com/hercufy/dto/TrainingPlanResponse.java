package com.hercufy.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TrainingPlanResponse(
        UUID id,
        String name,
        boolean active,
        String scheduleType,
        List<PlanDayResponse> days,
        Instant createdAt,
        Instant updatedAt
) {
}
