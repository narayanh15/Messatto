package com.messatto.service;

import com.messatto.domain.entity.AppUser;
import com.messatto.dto.auth.AuthResponse;
import com.messatto.dto.auth.LoginRequest;
import com.messatto.exception.UnauthorizedException;
import com.messatto.repository.UserRepository;
import com.messatto.security.JwtService;
import com.messatto.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        AppUser user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!user.isActive() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        UserPrincipal principal = UserPrincipal.from(user);
        String token = jwtService.generateToken(principal);
        log.info("User logged in: email={}, role={}", user.getEmail(), user.getRole());
        return new AuthResponse(token, "Bearer", jwtService.getExpirationMillis(), UserMapper.toResponse(user));
    }
}
