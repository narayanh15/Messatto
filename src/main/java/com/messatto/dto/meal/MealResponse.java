package com.messatto.dto.meal;

import com.messatto.domain.enums.MealType;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record MealResponse(
        UUID id,
        LocalDate mealDate,
        MealType mealType,
        String vegMenu,
        String nonVegMenu,
        LocalTime startTime,
        LocalTime endTime,
        String createdBy,
        Instant createdAt
) {
}
