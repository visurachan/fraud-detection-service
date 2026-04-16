package com.banking.fraud_detection_service.rule.impl;


import com.banking.fraud_detection_service.alert.FraudRuleResult;
import com.banking.fraud_detection_service.kafka.event.TransactionCreatedEvent;
import com.banking.fraud_detection_service.rule.FraudRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZoneOffset;

@Component
public class OffHoursRule implements FraudRule {

    private static final LocalTime OFF_HOURS_START = LocalTime.of(1, 0);
    private static final LocalTime OFF_HOURS_END = LocalTime.of(4, 0);
    private static final BigDecimal THRESHOLD = new BigDecimal("1000");

    @Override
    public FraudRuleResult evaluate(TransactionCreatedEvent event) {
        LocalTime transactionTime = event.getTimestamp()
                .atOffset(ZoneOffset.UTC)
                .toLocalTime();

        boolean isOffHours = !transactionTime.isBefore(OFF_HOURS_START)
                && transactionTime.isBefore(OFF_HOURS_END);

        boolean isLargeAmount = event.getAmount().compareTo(THRESHOLD) > 0;

        boolean triggered = isOffHours && isLargeAmount;

        return FraudRuleResult.builder()
                .ruleName(getRuleName())
                .triggered(triggered)
                .reason(triggered ? "Transaction of £" + event.getAmount() +
                                    " detected between 1am-4am" : null)
                .build();
    }

    @Override
    public String getRuleName() {
        return "OFF_HOURS";
    }
}