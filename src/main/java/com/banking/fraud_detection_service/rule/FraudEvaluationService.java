package com.banking.fraud_detection_service.rule;

import com.banking.fraud_detection_service.alert.FraudAlert;
import com.banking.fraud_detection_service.alert.FraudAlertRepository;
import com.banking.fraud_detection_service.alert.FraudRuleResult;
import com.banking.fraud_detection_service.alert.FraudRuleResultRepository;
import com.banking.fraud_detection_service.alert.AlertStatus;
import com.banking.fraud_detection_service.alert.Severity;
import com.banking.fraud_detection_service.kafka.event.TransactionCreatedEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class FraudEvaluationService {

    private final List<FraudRule> fraudRules;
    private final FraudAlertRepository fraudAlertRepository;
    private final FraudRuleResultRepository fraudRuleResultRepository;
    private final MeterRegistry meterRegistry;
    private final Counter kafkaMessagesConsumed;
    private final Counter kafkaMessagesFailed;

    public FraudEvaluationService(List<FraudRule> fraudRules,
                                  FraudAlertRepository fraudAlertRepository,
                                  FraudRuleResultRepository fraudRuleResultRepository,
                                  MeterRegistry meterRegistry) {
        this.fraudRules = fraudRules;
        this.fraudAlertRepository = fraudAlertRepository;
        this.fraudRuleResultRepository = fraudRuleResultRepository;
        this.meterRegistry = meterRegistry;

        this.kafkaMessagesConsumed = Counter.builder("fraud.kafka.messages.consumed")
                .description("Total Kafka messages consumed by fraud service")
                .register(meterRegistry);

        this.kafkaMessagesFailed = Counter.builder("fraud.kafka.messages.failed")
                .description("Total Kafka messages that failed processing")
                .register(meterRegistry);
    }

    @Transactional
    public void evaluate(TransactionCreatedEvent event) {
        kafkaMessagesConsumed.increment();
        log.info("Evaluating transaction: transactionId={}", event.getTransactionId());

        try {
            List<FraudRuleResult> results = fraudRules.stream()
                    .map(rule -> rule.evaluate(event))
                    .toList();

            boolean anyTriggered = results.stream()
                    .anyMatch(FraudRuleResult::isTriggered);

            if (!anyTriggered) {
                log.info("No fraud detected for transactionId={}", event.getTransactionId());
                return;
            }

            Severity severity = determineSeverity(results);
            String triggeredRuleName = results.stream()
                    .filter(FraudRuleResult::isTriggered)
                    .map(FraudRuleResult::getRuleName)
                    .findFirst()
                    .orElse("UNKNOWN");

            // increment new fraud counter tagged by rule and severity
            Counter.builder("fraud.alerts.triggered")
                    .tag("rule", triggeredRuleName)
                    .tag("severity", severity.name())
                    .description("Total fraud alerts triggered")
                    .register(meterRegistry)
                    .increment();

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

            log.warn("Fraud alert created: transactionId={}, severity={}, rule={}",
                    event.getTransactionId(), severity, triggeredRuleName);

        } catch (Exception e) {
            kafkaMessagesFailed.increment();
            log.error("Failed to evaluate transaction: transactionId={}, error={}",
                    event.getTransactionId(), e.getMessage());
            throw e;
        }
    }

    private Severity determineSeverity(List<FraudRuleResult> results) {
        long triggeredCount = results.stream()
                .filter(FraudRuleResult::isTriggered)
                .count();

        if (triggeredCount >= 2) return Severity.HIGH;

        String triggeredRule = results.stream()
                .filter(FraudRuleResult::isTriggered)
                .map(FraudRuleResult::getRuleName)
                .findFirst()
                .orElse("");

        return switch (triggeredRule) {
            case "LARGE_AMOUNT", "VELOCITY_CHECK" -> Severity.HIGH;
            case "DUPLICATE_TRANSACTION", "ROUND_AMOUNT" -> Severity.MEDIUM;
            case "OFF_HOURS" -> Severity.LOW;
            default -> Severity.MEDIUM;
        };
    }
}