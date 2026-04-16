package com.banking.fraud_detection_service.controller.dto;


import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class FraudStatsResponse {
    private long totalAlerts;
    private long openAlerts;
    private Map<String, Long> alertsBySeverity;
    private Map<String, Long> alertsByRule;
}