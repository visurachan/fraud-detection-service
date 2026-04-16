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
public class VelocityRule implements FraudRule {

    private static final int MAX_TRANSACTIONS = 3;
    private static final Duration WINDOW = Duration.ofSeconds(60);

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public FraudRuleResult evaluate(TransactionCreatedEvent event) {
        String key = "velocity::" + event.getSourceAccountId();

        Long count = redisTemplate.opsForValue().increment(key);

        if (count == 1) {
            redisTemplate.expire(key, WINDOW);
        }

        boolean triggered = count > MAX_TRANSACTIONS;

        return FraudRuleResult.builder()
                .ruleName(getRuleName())
                .triggered(triggered)
                .reason(triggered ? "Account made " + count + " transactions in 60 seconds" : null)
                .build();
    }

    @Override
    public String getRuleName() {
        return "VELOCITY_CHECK";
    }
}