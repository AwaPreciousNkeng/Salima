package com.codewithpcodes.salima.claim;

import com.codewithpcodes.salima.user.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/claims")
@RequiredArgsConstructor
@Tag(name = "Claims", description = "Claims Management Endpoints")
public class ClaimController {

    private final ClaimService claimService;

    @PostMapping("/subscriptions/{subscription-Id}")
    public ResponseEntity<ClaimResponse> submitClaim(
            @PathVariable("subscription-Id") UUID subscriptionId,
            @Valid @RequestBody SubmitClaimRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(claimService.submitClaim(currentUser.getId(), subscriptionId, request));
    }

    @GetMapping("/{claimId}")
    public ResponseEntity<ClaimResponse> getClaimById(
            @PathVariable UUID claimId
    ) {
        return ResponseEntity.ok(claimService.getClaimById(claimId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ClaimResponse>> getMyClaims(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(claimService.getClaimsByUserId(currentUser.getId()));
    }

    @GetMapping("/subscriptions/{subscriptionId}")
    public ResponseEntity<List<ClaimResponse>> getClaimsBySubscription(
            @PathVariable UUID subscriptionId
    ) {
        return ResponseEntity.ok(claimService.getClaimsBySubscriptionId(subscriptionId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{claimId}/approve")
    public ResponseEntity<ClaimResponse> approveClaim(
            @PathVariable UUID claimId,
            @Valid @RequestBody ApproveClaimRequest request
    ) {
        return ResponseEntity.ok(claimService.approveClaim(claimId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{claimId}/reject")
    public ResponseEntity<ClaimResponse> rejectClaim(
            @PathVariable UUID claimId,
            @RequestParam(required = false) String reason
    ) {
        return ResponseEntity.ok(claimService.rejectClaim(claimId, reason));
    }
}
