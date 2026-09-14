package com.messatto.repository;

import com.messatto.domain.entity.QrToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QrTokenRepository extends JpaRepository<QrToken, UUID> {

    Optional<QrToken> findByTokenHashAndActiveTrue(String tokenHash);
}
