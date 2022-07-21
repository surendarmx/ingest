package com.bt.orchestration.ingest.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.bt.orchestration.ingest.model.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static java.util.UUID.randomUUID;

@Component
@Slf4j
public class OrderStatusDao {

    private final DynamoDBMapper dynamoDBMapper;

    public OrderStatusDao(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public void saveMessage(String message) {
        OrderStatus orderStatus = OrderStatus.builder().orderId(randomUUID().toString()).message(message).build();
        dynamoDBMapper.save(orderStatus);
        log.info("Saved order to dynamo '{}'", orderStatus);
    }
}
