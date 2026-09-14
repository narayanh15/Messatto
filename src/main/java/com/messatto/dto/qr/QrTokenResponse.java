package com.messatto.dto.qr;

import com.messatto.domain.enums.MealChoice;
import java.time.Instant;
import java.util.UUID;

public record QrTokenResponse(
        UUID qrTokenId,
        UUID mealId,
        MealChoice mealChoice,
        String token,
        String qrPayload,
        Instant expiresAt
) {
}
