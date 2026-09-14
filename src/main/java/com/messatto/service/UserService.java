package com.messatto.service;

import com.messatto.domain.entity.AppUser;
import com.messatto.domain.enums.Role;
import com.messatto.dto.user.CreateUserRequest;
import com.messatto.dto.user.UserResponse;
import com.messatto.exception.BadRequestException;
import com.messatto.exception.ConflictException;
import com.messatto.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        String email = request.email().trim().toLowerCase();
        String rollNumber = normalizeOptional(request.rollNumber());

        if (request.role() == Role.STUDENT && rollNumber == null) {
            throw new BadRequestException("rollNumber is required for student accounts");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("A user with this email already exists");
        }
        if (rollNumber != null && userRepository.existsByRollNumber(rollNumber)) {
            throw new ConflictException("A user with this roll number already exists");
        }

        AppUser user = new AppUser(
                request.name().trim(),
                email,
                rollNumber,
                passwordEncoder.encode(request.password()),
                request.role(),
                normalizeOptional(request.hostel()),
                request.active() == null || request.active()
        );
        AppUser saved = userRepository.save(user);
        log.info("Created user: id={}, email={}, role={}", saved.getId(), saved.getEmail(), saved.getRole());
        return UserMapper.toResponse(saved);
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
