package com.bt.orchestration.ingest.listener;

import com.amazonaws.services.sqs.AmazonSQS;
import com.bt.orchestration.ingest.dao.OrderStatusDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaMessageListener {

    private final OrderStatusDao orderStatusDao;
    private final AmazonSQS amazonSQS;
    private final String conductorQueueUrl;

    public KafkaMessageListener(OrderStatusDao orderStatusDao, AmazonSQS amazonSQS,
                                @Value("${conductor.queue.url}") String conductorQueueUrl) {
        this.orderStatusDao = orderStatusDao;
        this.amazonSQS = amazonSQS;
        this.conductorQueueUrl = conductorQueueUrl;
    }

    @KafkaListener(topics = "execute-workflow")
    public void processEvent(String message) {
        log.info("Received Message in group execute-group '{}'", message);
        orderStatusDao.saveMessage(message);
        amazonSQS.sendMessage(conductorQueueUrl, message);
        log.info("Sent message to SQS");
    }
}
