package com.messatto.dto.user;

import com.messatto.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Email @Size(max = 180) String email,
        @Size(max = 60) String rollNumber,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotNull Role role,
        @Size(max = 120) String hostel,
        Boolean active
) {
}
