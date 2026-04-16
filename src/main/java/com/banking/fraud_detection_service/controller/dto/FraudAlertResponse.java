package com.banking.fraud_detection_service.controller.dto;

import com.banking.fraud_detection_service.alert.AlertStatus;
import com.banking.fraud_detection_service.alert.Severity;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class FraudAlertResponse {
    private Long id;
    private String transactionId;
    private String accountId;
    private BigDecimal amount;
    private String ruleTriggered;
    private Severity severity;
    private AlertStatus status;
    private Instant createdAt;
}
