package com.messatto.dto.meal;

import com.messatto.domain.enums.MealType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public record CreateMealRequest(
        @NotNull LocalDate mealDate,
        @NotNull MealType mealType,
        @NotBlank @Size(max = 2000) String vegMenu,
        @NotBlank @Size(max = 2000) String nonVegMenu,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime
) {
}
