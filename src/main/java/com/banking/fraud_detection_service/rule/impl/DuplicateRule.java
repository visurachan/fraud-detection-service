package com.banking.fraud_detection_service.rule.impl;

import com.banking.fraud_detection_service.alert.FraudRuleResult;
import com.banking.fraud_detection_service.kafka.event.TransactionCreatedEvent;
import com.banking.fraud_detection_service.rule.FraudRule;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class DuplicateRule implements FraudRule {

    private static final Duration WINDOW = Duration.ofMinutes(5);
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public FraudRuleResult evaluate(TransactionCreatedEvent event) {
        String key = "duplicate::" +
                event.getSourceAccountId() + "::" +
                event.getDestinationAccountId() + "::" +
                event.getAmount();

        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(key, "1", WINDOW);

        boolean triggered = Boolean.FALSE.equals(isNew);

        return FraudRuleResult.builder()
                .ruleName(getRuleName())
                .triggered(triggered)
                .reason(triggered ? "Duplicate transaction detected — same amount to same recipient within 5 minutes" : null)
                .build();
    }

    @Override
    public String getRuleName() {
        return "DUPLICATE_TRANSACTION";
    }
}