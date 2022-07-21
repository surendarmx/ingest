package com.bt.orchestration.ingest.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
@DynamoDBTable(tableName = "OrchestrationOrderStatus")
public class OrderStatus {

    @DynamoDBHashKey(attributeName = "OrderId")
    private String orderId;

    @DynamoDBAttribute(attributeName = "Message")
    private String message;
}
