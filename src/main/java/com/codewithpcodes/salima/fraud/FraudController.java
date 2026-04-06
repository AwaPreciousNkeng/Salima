package com.codewithpcodes.salima.fraud;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fraud")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Fraud Detection", description = "Fraud Detection Endpoints")
public class FraudController {

    private final FraudService fraudService;

    @PostMapping("/evaluate/{claimId}")
    public ResponseEntity<FraudScoreResponse> evaluateClaim(
            @PathVariable UUID claimId
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(fraudService.evaluateClaim(claimId));
    }

    @GetMapping("/claims/{claimId}")
    public ResponseEntity<FraudScoreResponse> getFraudScoreByClaimId(
            @PathVariable UUID claimId
    ) {
        return ResponseEntity.ok(fraudService.getFraudScoreByClaimId(claimId));
    }

    @GetMapping("/{fraudScoreId}")
    public ResponseEntity<FraudScoreResponse> getFraudScoreById(
            @PathVariable UUID fraudScoreId
    ) {
        return ResponseEntity.ok(fraudService.getFraudScoreById(fraudScoreId));
    }
}
