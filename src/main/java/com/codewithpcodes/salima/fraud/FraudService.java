package com.codewithpcodes.salima.fraud;

import com.codewithpcodes.salima.claim.Claim;
import com.codewithpcodes.salima.claim.ClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FraudService {
    private final FraudRepository fraudRepository;
    private final ClaimRepository claimRepository;

    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("5000");
    private static final int FREQUENT_CLAIMS_THRESHOLD = 3;
    private static final int FREQUENT_CLAIMS_DAYS = 30;

    @Transactional
    public FraudScoreResponse evaluateClaim(UUID claimId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalStateException("Claim not found"));

        if (fraudRepository.existsByClaimId(claimId)) {
            throw new IllegalStateException("Fraud score already exists for this claim");
        }

        int score = 0;
        List<String> reasons = new ArrayList<>();

        // Rule 1: High claim amount
        if (claim.getAmountRequested().compareTo(HIGH_AMOUNT_THRESHOLD) > 0) {
            score += 30;
            reasons.add("High claim amount exceeds threshold");
        }

        // Rule 2: Frequent claims in short period
        List<Claim> recentClaims = claimRepository.findAllByUserId(claim.getUser().getId()).stream()
                .filter(c -> c.getSubmittedAt() != null &&
                        c.getSubmittedAt().isAfter(LocalDateTime.now().minusDays(FREQUENT_CLAIMS_DAYS)))
                .toList();

        if (recentClaims.size() >= FREQUENT_CLAIMS_THRESHOLD) {
            score += 25;
            reasons.add("Multiple claims submitted within " + FREQUENT_CLAIMS_DAYS + " days");
        }

        // Rule 3: Claim submitted shortly after subscription
        if (claim.getSubscription().getCreatedAt() != null) {
            long daysSinceSubscription = ChronoUnit.DAYS.between(
                    claim.getSubscription().getCreatedAt(),
                    claim.getSubmittedAt()
            );
            if (daysSinceSubscription < claim.getSubscription().getWaitingPeriodInDays()) {
                score += 40;
                reasons.add("Claim submitted during waiting period");
            } else if (daysSinceSubscription < 14) {
                score += 20;
                reasons.add("Claim submitted within 14 days of subscription");
            }
        }

        // Rule 4: Claim amount close to or at limit
        BigDecimal claimLimit = claim.getSubscription().getClaimLimit();
        if (claim.getAmountRequested().compareTo(claimLimit.multiply(new BigDecimal("0.9"))) >= 0) {
            score += 15;
            reasons.add("Claim amount is near or at subscription limit");
        }

        // Determine risk level
        String riskLevel = determineRiskLevel(score);
        String reasonText = reasons.isEmpty() ? "No risk factors detected" : String.join("; ", reasons);

        FraudScore fraudScore = FraudScore.builder()
                .score(score)
                .riskLevel(riskLevel)
                .reason(reasonText)
                .claim(claim)
                .evaluatedAt(LocalDateTime.now())
                .build();

        FraudScore savedScore = fraudRepository.save(fraudScore);
        return FraudScoreResponse.fromFraudScore(savedScore);
    }

    public FraudScoreResponse getFraudScoreByClaimId(UUID claimId) {
        FraudScore fraudScore = fraudRepository.findByClaimId(claimId)
                .orElseThrow(() -> new IllegalStateException("Fraud score not found for this claim"));
        return FraudScoreResponse.fromFraudScore(fraudScore);
    }

    public FraudScoreResponse getFraudScoreById(UUID fraudScoreId) {
        FraudScore fraudScore = fraudRepository.findById(fraudScoreId)
                .orElseThrow(() -> new IllegalStateException("Fraud score not found"));
        return FraudScoreResponse.fromFraudScore(fraudScore);
    }

    private String determineRiskLevel(int score) {
        if (score >= 70) {
            return RiskLevel.CRITICAL.name();
        } else if (score >= 50) {
            return RiskLevel.HIGH.name();
        } else if (score >= 25) {
            return RiskLevel.MEDIUM.name();
        } else {
            return RiskLevel.LOW.name();
        }
    }
}
