package com.codewithpcodes.salima.claim;

import com.codewithpcodes.salima.fraud.FraudScore;
import com.codewithpcodes.salima.payment.Payout;
import com.codewithpcodes.salima.provider.Provider;
import com.codewithpcodes.salima.subscription.Subscription;
import com.codewithpcodes.salima.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "claims")
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private BigDecimal amountRequested;

    private BigDecimal amountApproved;

    @Enumerated(EnumType.STRING)
    private ClaimStatus claimStatus;

    private String description;
    private LocalDate treatmentDate;
    private LocalDateTime processedAt;


    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL)
    private List<ClaimDocument> documents;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @OneToOne(fetch = FetchType.LAZY)
    private Provider provider;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Payout> payout;

    private LocalDateTime submittedAt;
}
