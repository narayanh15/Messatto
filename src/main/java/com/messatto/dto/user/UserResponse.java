package com.messatto.dto.user;

import com.messatto.domain.enums.Role;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        String rollNumber,
        Role role,
        String hostel,
        boolean active,
        Instant createdAt
) {
}
