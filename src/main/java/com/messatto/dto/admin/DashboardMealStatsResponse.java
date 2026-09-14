package com.messatto.dto.admin;

import com.messatto.domain.enums.MealType;
import java.time.LocalDate;
import java.util.UUID;

public record DashboardMealStatsResponse(
        UUID mealId,
        LocalDate mealDate,
        MealType mealType,
        long totalAttendance,
        long vegCount,
        long nonVegCount
) {
}
