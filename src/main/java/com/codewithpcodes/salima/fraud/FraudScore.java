package com.codewithpcodes.salima.fraud;

import com.codewithpcodes.salima.claim.Claim;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "fraud_scores")
public class FraudScore {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private int score;
    private String riskLevel;
    private String reason;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    private LocalDateTime evaluatedAt;

}
