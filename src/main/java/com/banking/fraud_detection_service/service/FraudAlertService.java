package com.banking.fraud_detection_service.service;


import com.banking.fraud_detection_service.alert.FraudAlert;
import com.banking.fraud_detection_service.alert.FraudAlertRepository;
import com.banking.fraud_detection_service.alert.AlertStatus;
import com.banking.fraud_detection_service.controller.dto.FraudAlertResponse;
import com.banking.fraud_detection_service.controller.dto.FraudStatsResponse;
import com.banking.fraud_detection_service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FraudAlertService {

    private final FraudAlertRepository fraudAlertRepository;

    public Page<FraudAlertResponse> getAllAlerts(Pageable pageable) {
        return fraudAlertRepository.findAll(pageable)
                .map(this::toResponse);
    }

    public FraudAlertResponse getAlertById(Long id) {
        FraudAlert alert = fraudAlertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + id));
        return toResponse(alert);
    }

    public List<FraudAlertResponse> getAlertsByAccount(String accountId) {
        return fraudAlertRepository.findByAccountId(accountId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public FraudAlertResponse updateAlertStatus(Long id, AlertStatus newStatus) {
        FraudAlert alert = fraudAlertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + id));
        alert.setStatus(newStatus);
        return toResponse(fraudAlertRepository.save(alert));
    }

    public FraudStatsResponse getStats() {
        List<FraudAlert> allAlerts = fraudAlertRepository.findAll();

        Map<String, Long> bySeverity = allAlerts.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getSeverity().name(),
                        Collectors.counting()
                ));

        Map<String, Long> byRule = allAlerts.stream()
                .collect(Collectors.groupingBy(
                        FraudAlert::getRuleTriggered,
                        Collectors.counting()
                ));

        long openAlerts = allAlerts.stream()
                .filter(a -> a.getStatus() == AlertStatus.OPEN)
                .count();

        return FraudStatsResponse.builder()
                .totalAlerts(allAlerts.size())
                .openAlerts(openAlerts)
                .alertsBySeverity(bySeverity)
                .alertsByRule(byRule)
                .build();
    }

    private FraudAlertResponse toResponse(FraudAlert alert) {
        return FraudAlertResponse.builder()
                .id(alert.getId())
                .transactionId(alert.getTransactionId())
                .accountId(alert.getAccountId())
                .amount(alert.getAmount())
                .ruleTriggered(alert.getRuleTriggered())
                .severity(alert.getSeverity())
                .status(alert.getStatus())
                .createdAt(alert.getCreatedAt())
                .build();
    }
}
