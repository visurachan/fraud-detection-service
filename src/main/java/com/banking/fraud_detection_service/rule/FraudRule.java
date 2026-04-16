package com.banking.fraud_detection_service.rule;

import com.banking.fraud_detection_service.alert.FraudRuleResult;
import com.banking.fraud_detection_service.kafka.event.TransactionCreatedEvent;

public interface FraudRule {
    FraudRuleResult evaluate(TransactionCreatedEvent event);
    String getRuleName();
}
