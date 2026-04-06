package com.codewithpcodes.salima.claim;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClaimRepository extends JpaRepository<Claim, UUID> {

    List<Claim> findAllByUserId(UUID userId);
    List<Claim> findAllBySubscriptionId(UUID subscriptionId);
}
