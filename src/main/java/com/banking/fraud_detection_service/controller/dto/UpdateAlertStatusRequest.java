package com.banking.fraud_detection_service.controller.dto;

import com.banking.fraud_detection_service.alert.AlertStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAlertStatusRequest {
    @NotNull
    private AlertStatus status;
}
