package com.messatto.dto.attendance;

import com.messatto.domain.enums.MealChoice;
import com.messatto.domain.enums.MealType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ScanResponse(
        UUID attendanceId,
        UUID mealId,
        MealType mealType,
        LocalDate mealDate,
        MealChoice mealChoice,
        Instant scannedAt,
        boolean alreadyRecorded
) {
}
