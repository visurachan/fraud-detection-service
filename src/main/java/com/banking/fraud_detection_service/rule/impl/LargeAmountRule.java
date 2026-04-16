package com.banking.fraud_detection_service.rule.impl;

import com.banking.fraud_detection_service.alert.FraudRuleResult;
import com.banking.fraud_detection_service.kafka.event.TransactionCreatedEvent;
import com.banking.fraud_detection_service.rule.FraudRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class LargeAmountRule implements FraudRule {

    private static final BigDecimal THRESHOLD = new BigDecimal("5000");

    @Override
    public FraudRuleResult evaluate(TransactionCreatedEvent event) {
        boolean triggered = event.getAmount().compareTo(THRESHOLD)>0;

        return FraudRuleResult.builder()
                .ruleName(getRuleName())
                .triggered(triggered)
                .reason(triggered ? "Amount £" + event.getAmount()+" exceeds threshold of £5000" : null)
                .build();
    }



    @Override
    public String getRuleName() {
        return "LARGE_AMOUNT";
    }
}
