package com.codewithpcodes.salima.claim;

import java.math.BigDecimal;

public record ApproveClaimRequest(
        BigDecimal amountApproved
) {
}
