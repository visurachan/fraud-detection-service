package com.banking.fraud_detection_service.kafka.consumer;


import com.banking.fraud_detection_service.kafka.event.TransactionCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransactionEventConsumer {

    @KafkaListener(
            topics = "transaction.created",
            groupId = "fraud-detection-group"
    )
    public void consume(
            @Payload TransactionCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info("Received transaction event: transactionId={}, amount={}, partition={}, offset={}",
                event.getTransactionId(),
                event.getAmount(),
                partition,
                offset
        );
    }
}