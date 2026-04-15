package com.banking.fraud_detection_service.alert;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fraud_rule_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudRuleResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fraud_alert_id", nullable = false)
    private FraudAlert fraudAlert;

    @Column(nullable = false)
    private String ruleName;

    @Column(nullable = false)
    private boolean triggered;

    @Column
    private String reason;
}