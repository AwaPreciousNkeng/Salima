package com.codewithpcodes.salima.fraud;

import java.time.LocalDateTime;
import java.util.UUID;

public record FraudScoreResponse(
        UUID id,
        UUID claimId,
        int score,
        String riskLevel,
        String reason,
        LocalDateTime evaluatedAt
) {
    public static FraudScoreResponse fromFraudScore(FraudScore fraudScore) {
        return new FraudScoreResponse(
                fraudScore.getId(),
                fraudScore.getClaim().getId(),
                fraudScore.getScore(),
                fraudScore.getRiskLevel(),
                fraudScore.getReason(),
                fraudScore.getEvaluatedAt()
        );
    }
}
