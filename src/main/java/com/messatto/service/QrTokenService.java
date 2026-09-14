package com.messatto.service;

import com.messatto.domain.entity.Meal;
import com.messatto.domain.entity.QrToken;
import com.messatto.domain.enums.MealChoice;
import com.messatto.dto.qr.GenerateQrRequest;
import com.messatto.dto.qr.QrTokenResponse;
import com.messatto.exception.BadRequestException;
import com.messatto.exception.NotFoundException;
import com.messatto.repository.MealRepository;
import com.messatto.repository.QrTokenRepository;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QrTokenService {

    private static final Logger log = LoggerFactory.getLogger(QrTokenService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String REDIS_PREFIX = "messatto:qr:";

    private final MealRepository mealRepository;
    private final QrTokenRepository qrTokenRepository;
    private final StringRedisTemplate redisTemplate;
    private final Clock clock;
    private final int defaultTtlSeconds;

    public QrTokenService(
            MealRepository mealRepository,
            QrTokenRepository qrTokenRepository,
            StringRedisTemplate redisTemplate,
            Clock clock,
            @Value("${app.qr.ttl-seconds:900}") int defaultTtlSeconds
    ) {
        this.mealRepository = mealRepository;
        this.qrTokenRepository = qrTokenRepository;
        this.redisTemplate = redisTemplate;
        this.clock = clock;
        this.defaultTtlSeconds = defaultTtlSeconds;
    }

    @Transactional
    public QrTokenResponse generateToken(UUID mealId, GenerateQrRequest request) {
        Meal meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new NotFoundException("Meal not found"));

        Instant now = Instant.now(clock);
        Instant mealEnd = LocalDateTime.of(meal.getMealDate(), meal.getEndTime()).atZone(clock.getZone()).toInstant();
        if (now.isAfter(mealEnd)) {
            throw new BadRequestException("Cannot generate QR token after the meal window has ended");
        }

        int ttlSeconds = request.ttlSeconds() == null ? defaultTtlSeconds : request.ttlSeconds();
        Instant expiresAt = now.plusSeconds(ttlSeconds);
        if (expiresAt.isAfter(mealEnd)) {
            expiresAt = mealEnd;
        }
        Duration ttl = Duration.between(now, expiresAt);
        if (ttl.isZero() || ttl.isNegative()) {
            throw new BadRequestException("QR token expiry must be in the future");
        }

        String rawToken = generateRawToken();
        String tokenHash = sha256(rawToken);
        QrToken saved = qrTokenRepository.save(new QrToken(meal, request.mealChoice(), tokenHash, expiresAt));
        redisTemplate.opsForValue().set(
                redisKey(tokenHash),
                meal.getId() + "|" + request.mealChoice().name() + "|" + expiresAt,
                ttl
        );

        log.info("Generated QR token: qrTokenId={}, mealId={}, choice={}, expiresAt={}", saved.getId(), mealId, request.mealChoice(), expiresAt);
        return new QrTokenResponse(
                saved.getId(),
                meal.getId(),
                request.mealChoice(),
                rawToken,
                "messatto://attendance/scan?token=" + rawToken,
                expiresAt
        );
    }

    @Transactional(readOnly = true)
    public ValidatedQrToken validateToken(String tokenOrPayload) {
        String token = normalizeToken(tokenOrPayload);
        String tokenHash = sha256(token);

        String redisPayload = redisTemplate.opsForValue().get(redisKey(tokenHash));
        if (redisPayload == null) {
            throw new BadRequestException("QR token is invalid or expired");
        }

        QrToken qrToken = qrTokenRepository.findByTokenHashAndActiveTrue(tokenHash)
                .orElseThrow(() -> new BadRequestException("QR token is invalid or inactive"));

        if (!qrToken.getExpiresAt().isAfter(Instant.now(clock))) {
            throw new BadRequestException("QR token is expired");
        }

        return new ValidatedQrToken(qrToken.getMeal(), qrToken.getMealChoice());
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String normalizeToken(String value) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException("QR token is required");
        }
        String raw = value.trim();
        if (!raw.contains("token=")) {
            return raw;
        }
        try {
            URI uri = URI.create(raw);
            String query = uri.getRawQuery();
            if (query == null) {
                return raw;
            }
            for (String part : query.split("&")) {
                String[] pair = part.split("=", 2);
                if (pair.length == 2 && "token".equals(pair[0])) {
                    return URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                }
            }
            return raw;
        } catch (IllegalArgumentException ex) {
            int tokenIndex = raw.indexOf("token=");
            return raw.substring(tokenIndex + "token=".length());
        }
    }

    private String redisKey(String tokenHash) {
        return REDIS_PREFIX + tokenHash;
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    public record ValidatedQrToken(Meal meal, MealChoice mealChoice) {
    }
}
