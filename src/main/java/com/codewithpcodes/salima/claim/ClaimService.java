package com.codewithpcodes.salima.claim;

import com.codewithpcodes.salima.subscription.Subscription;
import com.codewithpcodes.salima.subscription.SubscriptionRepository;
import com.codewithpcodes.salima.subscription.SubscriptionService;
import com.codewithpcodes.salima.subscription.SubscriptionStatus;
import com.codewithpcodes.salima.user.User;
import com.codewithpcodes.salima.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    public ClaimResponse submitClaim(UUID userId, UUID subscriptionId, SubmitClaimRequest request) {

        //Validate User exists
        User user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalStateException("User not found"));

        //Validate subscription exists and is active
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalStateException("Subscription not found"));

        if (subscription.getSubscriptionStatus() != SubscriptionStatus.ACTIVE) {
            throw new IllegalStateException("Subscription is not active");
        }

        //Validate subscription belongs to the user
        if (!subscription.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Subscription does not belong to this user");
        }

        //Check if claim amount exceeds the remaining claim limit
        var remainingLimit = subscription.getClaimLimit()
                .subtract(subscription.getClaimedAmountThisMonth());
        if (request.amountRequested().compareTo(remainingLimit) > 0) {
            throw new IllegalStateException("Claim amount exceeds remaining claim limit for this month");
        }

        //Create and save the claim
        Claim claim = Claim.builder()
                .amountRequested(request.amountRequested())
                .claimStatus(ClaimStatus.PENDING)
                .description(request.description())
                .treatmentDate(request.treatmentDate())
                .user(user)
                .subscription(subscription)
                .submittedAt(LocalDateTime.now())
                .build();

        Claim savedClaim = claimRepository.save(claim);
        return ClaimResponse.fromClaim(savedClaim);
    }

    public ClaimResponse getClaimById(UUID claimId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalStateException("Claim not found"));
        return ClaimResponse.fromClaim(claim);
    }

    public List<ClaimResponse> getClaimsByUserId(UUID userId) {
        return claimRepository.findAllByUserId(userId).stream()
                .map(ClaimResponse::fromClaim)
                .toList();
    }

    public List<ClaimResponse> getClaimsBySubscriptionId(UUID subscriptionId) {
        return claimRepository.findAllBySubscriptionId(subscriptionId).stream()
                .map(ClaimResponse::fromClaim)
                .toList();
    }

    @Transactional
    public ClaimResponse approveClaim(UUID claimId, ApproveClaimRequest request) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalStateException("Claim not found"));

        if (claim.getClaimStatus() != ClaimStatus.PENDING) {
            throw new IllegalStateException("Only pending claims can be approved");
        }

        claim.setClaimStatus(ClaimStatus.APPROVED);
        claim.setAmountApproved(request.amountApproved());
        claim.setProcessedAt(LocalDateTime.now());

        //update the subscription's claimed amount
        Subscription subscription = claim.getSubscription();
        subscription.setClaimedAmountThisMonth(subscription.getClaimedAmountThisMonth().add(request.amountApproved()));

        subscriptionRepository.save(subscription);
        Claim savedClaim = claimRepository.save(claim);
        return ClaimResponse.fromClaim(savedClaim);
    }

    @Transactional
    public ClaimResponse rejectClaim(UUID claimId, String rejectionReason) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalStateException("Claim not found"));

        if (claim.getClaimStatus() != ClaimStatus.PENDING) {
            throw new IllegalStateException("Only pending claims can be rejected");
        }

        claim.setClaimStatus(ClaimStatus.REJECTED);
        claim.setProcessedAt(LocalDateTime.now());

        Claim savedClaim = claimRepository.save(claim);
        return ClaimResponse.fromClaim(savedClaim);
    }
}
