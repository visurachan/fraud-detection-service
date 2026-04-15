CREATE TABLE fraud_rule_results (
                                    id BIGSERIAL PRIMARY KEY,
                                    fraud_alert_id BIGINT NOT NULL REFERENCES fraud_alerts(id),
                                    rule_name VARCHAR(255) NOT NULL,
                                    triggered BOOLEAN NOT NULL,
                                    reason TEXT
);