package com.banking.fraud_detection_service.alert;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FraudRuleResultRepository extends JpaRepository<FraudRuleResult, Long> {
    List<FraudRuleResult> findByFraudAlertId(Long fraudAlertId);
}