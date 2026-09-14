package com.messatto.dto.feedback;

import java.time.Instant;

public record FeedbackCommentResponse(
        int tasteRating,
        int hygieneRating,
        int quantityRating,
        int serviceRating,
        String comment,
        boolean anonymous,
        String studentName,
        Instant createdAt
) {
}
