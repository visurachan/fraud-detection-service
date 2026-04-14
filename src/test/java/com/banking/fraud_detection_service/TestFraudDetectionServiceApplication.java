package com.banking.fraud_detection_service;

import org.springframework.boot.SpringApplication;

public class TestFraudDetectionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(FraudDetectionServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
