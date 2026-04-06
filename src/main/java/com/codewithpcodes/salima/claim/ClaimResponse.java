package com.codewithpcodes.salima.claim;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ClaimResponse(
        UUID id,
        BigDecimal amountRequested,
        BigDecimal amountApproved,
        ClaimStatus claimStatus,
        String description,
        LocalDateTime treatmentDate,
        LocalDateTime submittedAt,
        LocalDateTime processedAt
) {
    public static ClaimResponse fromClaim(Claim claim) {
        return new ClaimResponse(
                claim.getId(),
                claim.getAmountRequested(),
                claim.getAmountApproved(),
                claim.getClaimStatus(),
                claim.getDescription(),
                claim.getTreatmentDate(),
                claim.getSubmittedAt(),
                claim.getProcessedAt()
        );
    }
}
