package com.messatto.dto.feedback;

import java.time.Instant;
import java.util.UUID;

public record FeedbackResponse(
        UUID id,
        UUID mealId,
        Instant createdAt
) {
}
