package com.banking.fraud_detection_service.rule;

import com.banking.fraud_detection_service.alert.*;
import com.banking.fraud_detection_service.kafka.event.TransactionCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudEvaluationService {

    private final List<FraudRule> fraudRules;
    private final FraudAlertRepository fraudAlertRepository;
    private final FraudRuleResultRepository fraudRuleResultRepository;

    @Transactional
    public void evaluate(TransactionCreatedEvent event){
        log.info("Evalauating transaction: transaction = {}", event.getTransactionId());

        List<FraudRuleResult> results = fraudRules.stream()
                .map(rule -> rule.evaluate(event))
                .toList();

        boolean anyTriggered = results.stream()
                .anyMatch(FraudRuleResult::isTriggered);

        if (!anyTriggered){
            log.info("No fraud detected for transactionId={}", event.getTransactionId());
            return;
        }

        Severity severity = determineSeverity(results);
        String triggeredRuleName = results.stream()
                .filter(FraudRuleResult::isTriggered)
                .map(FraudRuleResult::getRuleName)
                .findFirst()
                .orElse("UNKNOWN");

        FraudAlert alert = FraudAlert.builder()
                .transactionId(event.getTransactionId())
                .accountId(event.getSourceAccountId())
                .amount(event.getAmount())
                .ruleTriggered(triggeredRuleName)
                .severity(severity)
                .status(AlertStatus.OPEN)
                .build();

        FraudAlert savedAlert = fraudAlertRepository.save(alert);

        results.forEach(result -> result.setFraudAlert(savedAlert));
        fraudRuleResultRepository.saveAll(results);

        log.warn("Fraud alert created: transactionId={}, severoty={}, rule={}",
                event.getTransactionId(),severity, triggeredRuleName);

    }

    private Severity determineSeverity(List<FraudRuleResult> results) {
        long triggeredCount = results.stream()
                .filter(FraudRuleResult::isTriggered)
                .count();

        if (triggeredCount >=2) return Severity.HIGH;

        String triggeredRule = results.stream()
                .filter(FraudRuleResult::isTriggered)
                .map(FraudRuleResult::getRuleName)
                .findFirst()
                .orElse("");

        return switch (triggeredRule) {
            case "LARGE_AMOUNT", "VELOCITY_CHECK" -> Severity.HIGH;
            case "DUPLICATE_TRANSACTION" -> Severity.MEDIUM;
            case "OFF_HOURS" -> Severity.LOW;
            default -> Severity.MEDIUM;
        };




        }

}
