CREATE TABLE fraud_alerts (
                              id BIGSERIAL PRIMARY KEY,
                              transaction_id VARCHAR(255) NOT NULL,
                              account_id VARCHAR(255) NOT NULL,
                              amount NUMERIC(19, 4) NOT NULL,
                              rule_triggered VARCHAR(255) NOT NULL,
                              severity VARCHAR(20) NOT NULL,
                              status VARCHAR(20) NOT NULL,
                              created_at TIMESTAMP WITH TIME ZONE NOT NULL
);