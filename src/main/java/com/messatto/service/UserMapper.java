package com.messatto.service;

import com.messatto.domain.entity.AppUser;
import com.messatto.dto.user.UserResponse;

final class UserMapper {

    private UserMapper() {
    }

    static UserResponse toResponse(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRollNumber(),
                user.getRole(),
                user.getHostel(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
