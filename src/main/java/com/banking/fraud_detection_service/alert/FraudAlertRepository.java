package com.banking.fraud_detection_service.alert;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FraudAlertRepository extends JpaRepository<FraudAlert, Long> {
    List<FraudAlert> findByAccountId(String accountId);
}
