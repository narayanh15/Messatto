package com.messatto.dto.attendance;

import com.messatto.domain.enums.MealChoice;
import com.messatto.domain.enums.MealType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AttendanceHistoryResponse(
        UUID attendanceId,
        UUID mealId,
        LocalDate mealDate,
        MealType mealType,
        MealChoice mealChoice,
        Instant scannedAt
) {
}
