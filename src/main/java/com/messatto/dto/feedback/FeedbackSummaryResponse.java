package com.messatto.dto.feedback;

import com.messatto.domain.enums.MealType;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record FeedbackSummaryResponse(
        UUID mealId,
        LocalDate mealDate,
        MealType mealType,
        long totalFeedback,
        double averageTasteRating,
        double averageHygieneRating,
        double averageQuantityRating,
        double averageServiceRating,
        List<FeedbackCommentResponse> comments
) {
}
