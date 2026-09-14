package com.messatto.dto.attendance;

import jakarta.validation.constraints.NotBlank;

public record ScanRequest(
        @NotBlank String token
) {
}
