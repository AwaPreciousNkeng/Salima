package com.codewithpcodes.salima.fraud;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FraudRepository extends JpaRepository<FraudScore, UUID> {
    Optional<FraudScore> findByClaimId(UUID claimId);
    boolean existsByClaimId(UUID claimId);
}
