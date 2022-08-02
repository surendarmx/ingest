package com.bt.orchestration.ingest.dao;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.bt.orchestration.ingest.model.CartDetails;
import com.bt.orchestration.ingest.entity.OrderStatusTracker;
import com.bt.orchestration.ingest.entity.Transactions;
import com.bt.orchestration.ingest.entity.WorkflowTracker;
import com.bt.orchestration.ingest.sqs.SQSMessageForwarder;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DynamoDBRepository {

	@Autowired
	private DynamoDBMapper dynamoDBMapper;

	@Autowired
	AmazonDynamoDB amazonDynamoDB;

	@Autowired
	SQSMessageForwarder sqsMessageForwarder;

	@Value("${sqs.conductor.name}")
	private String sqsWorkflowQueueName;

	public void saveOrderStatus(CartDetails cartDetails) {
		createDynamicTables(OrderStatusTracker.class, "OrderStatusTracker");

		OrderStatusTracker tracker = OrderStatusTracker.builder().orderId(cartDetails.getCartId())
				.status(cartDetails.getStatus()).itemDetails(cartDetails.getItemDetails()).build();

		log.info("Saving request to dynamo '{}'", tracker);
		dynamoDBMapper.save(tracker);
		log.info("Saved request to dynamo '{}'", tracker);

	}

	public void saveTransaction(Map<String, Object> mappedData) {
		createDynamicTables(Transactions.class, "Transactions");
		Transactions transaction = Transactions.builder().cartId((String) mappedData.get("cartId"))
				.requestDetails(mappedData).build();
		log.info("Saving request to dynamo DB '{}'", transaction);
		dynamoDBMapper.save(transaction);
		log.info("Saved request to dynamo DB '{}'", transaction);

	}

	public void saveWorkflowTracker(List<WorkflowTracker> workflowList) {
		createDynamicTables(WorkflowTracker.class, "WorkflowTracker");
		dynamoDBMapper.batchSave(workflowList);
		workflowList.forEach(e -> log.info("Saved order to dynamo '{}'", e));
	}
	
	private <T> void createDynamicTables(Class<T> tableClass, String tableName) {
		ListTablesResult tableList = amazonDynamoDB.listTables();
		boolean isTableExists = tableList.getTableNames().stream().anyMatch(e -> e.equalsIgnoreCase(tableName));
		if (!isTableExists) {
			log.info("Creating table in the dynamoDb {}", tableName);
			CreateTableRequest tableRequest = dynamoDBMapper.generateCreateTableRequest(tableClass);
			tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
			amazonDynamoDB.createTable(tableRequest);
			log.info("Created the table in the dynamoDb {}", tableName);
		}

	}

	/*
	 * public void updateMessage(Map<String, Object> mappedData) {
	 * log.info("Recevied message to update {}", mappedData); UpdateItemRequest
	 * updateItemRequest = new UpdateItemRequest().withTableName("OrderTracker")
	 * .addKeyEntry("CartId", new AttributeValue().withS((String)
	 * mappedData.get("orderId"))); if (mappedData.get("status").equals("failed")) {
	 * updateItemRequest.addKeyEntry("ItemId", new AttributeValue().withS((String)
	 * mappedData.get("itemId"))) .addAttributeUpdatesEntry("ProcessedQuantity", new
	 * AttributeValueUpdate() .withValue(new AttributeValue().withS((String)
	 * mappedData.get("quantity")))); } else {
	 * updateItemRequest.addKeyEntry("ItemId", new AttributeValue().withS((String)
	 * mappedData.get("itemId"))) .addAttributeUpdatesEntry("FailedQuantity", new
	 * AttributeValueUpdate() .withValue(new AttributeValue().withS((String)
	 * mappedData.get("quantity")))); } try {
	 * log.info("Updating DynamoDB with status: [{}] and request [{}]",
	 * mappedData.get("status"), updateItemRequest); UpdateItemResult
	 * updateItemResult = amazonDynamoDB.updateItem(updateItemRequest);
	 * log.info("Updated DynamoDB with {}", updateItemResult); } catch (Exception e)
	 * { log.info("Records not updated in DynamoDB due to {}", e.getMessage()); } }
	 */
}
