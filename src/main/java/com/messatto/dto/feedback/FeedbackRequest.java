package com.messatto.dto.feedback;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record FeedbackRequest(
        @NotNull UUID mealId,
        @Min(1) @Max(5) int tasteRating,
        @Min(1) @Max(5) int hygieneRating,
        @Min(1) @Max(5) int quantityRating,
        @Min(1) @Max(5) int serviceRating,
        @Size(max = 1000) String comment,
        boolean anonymous
) {
}
