package com.messatto.dto.qr;

import com.messatto.domain.enums.MealChoice;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GenerateQrRequest(
        @NotNull MealChoice mealChoice,
        @Min(60) @Max(3600) Integer ttlSeconds
) {
}
