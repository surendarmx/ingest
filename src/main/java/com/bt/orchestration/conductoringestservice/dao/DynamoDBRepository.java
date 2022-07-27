package com.bt.orchestration.conductoringestservice.dao;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.amazonaws.services.dynamodbv2.model.UpdateItemResult;
import com.bt.orchestration.conductoringestservice.model.CartDetails;
import com.bt.orchestration.conductoringestservice.model.ItemDetails;
import com.bt.orchestration.conductoringestservice.model.Transactions;
import com.bt.orchestration.conductoringestservice.model.WorkflowTracker;
import com.bt.orchestration.conductoringestservice.sqs.SQSMessageForwarder;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static java.util.UUID.randomUUID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

	public void saveMessage(Map<String, Object> mappedData) {
		saveRequest(mappedData);
		CartDetails cartDetails = new CartDetails();
		cartDetails.setCartId((String) mappedData.get("cartId"));
		cartDetails.setEvent((String) mappedData.get("eventName"));
		List<ItemDetails> itemList = new ArrayList<>();
		cartDetails.setItemDetails(itemList);
		if (mappedData.get("orderItems") != null) {
			List<Map<String, Object>> mappedItems = (List<Map<String, Object>>) mappedData.get("orderItems");
			mappedItems.stream().forEach(e -> {
				ItemDetails item = new ItemDetails();
				item.setProductId((String) e.get("productId"));
				item.setQuantity(String.valueOf(e.get("quantity")));
				itemList.add(item);
			});

		}

		ListTablesResult tableList = amazonDynamoDB.listTables();
		boolean isTableExists = tableList.getTableNames().stream().anyMatch(e -> e.equalsIgnoreCase("WorkflowTracker"));
		if (!isTableExists) {
			log.info("Creating table in the dynamoDb OrderTracker");
			CreateTableRequest tableRequest = dynamoDBMapper.generateCreateTableRequest(WorkflowTracker.class);
			tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
			amazonDynamoDB.createTable(tableRequest);
			log.info("created the table in the dynamoDb OrderTracker");
		}
		List<WorkflowTracker> workflowList = new ArrayList<>();
		cartDetails.getItemDetails().forEach(e -> {

			WorkflowTracker tracker = WorkflowTracker.builder().orderId(cartDetails.getCartId())
					.itemId(e.getProductId()).quantity(e.getQuantity()).eventStatus(cartDetails.getEvent()).build();
			workflowList.add(tracker);
			log.info("Saving order to dynamo '{}'", tracker);
			
			try {
				sqsMessageForwarder.pushMessage(tracker, sqsWorkflowQueueName);
			} catch (JsonProcessingException e1) {
				log.error("Error occured due to '{}'", e1.getMessage());
			}
		});
		
		dynamoDBMapper.batchSave(workflowList);
		workflowList.forEach(e->log.info("Saved order to dynamo '{}'", e));
		
	}

	public void saveRequest(Map<String, Object> mappedData) {

		ListTablesResult tableList = amazonDynamoDB.listTables();
		boolean isTableExists = tableList.getTableNames().stream().anyMatch(e -> e.equalsIgnoreCase("Transactions"));
		if (!isTableExists) {
			log.info("Creating table in the dynamoDb OrderTracker");
			CreateTableRequest tableRequest = dynamoDBMapper.generateCreateTableRequest(Transactions.class);
			tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
			amazonDynamoDB.createTable(tableRequest);
			log.info("created the table in the dynamoDb OrderTracker");
		}
		Transactions transaction = Transactions.builder().cartId((String) mappedData.get("cartId"))
				.requestDetails(mappedData).build();

		log.info("Saving request to dynamo '{}'", transaction);
		dynamoDBMapper.save(transaction);
		log.info("Saved request to dynamo '{}'", transaction);

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
