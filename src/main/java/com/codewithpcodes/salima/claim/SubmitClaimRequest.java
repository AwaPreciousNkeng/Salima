package com.codewithpcodes.salima.claim;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SubmitClaimRequest(
        UUID providerId,
        BigDecimal amountRequested,
        String description,
        LocalDate treatmentDate,
        List<String> documentUrls
) {
}
