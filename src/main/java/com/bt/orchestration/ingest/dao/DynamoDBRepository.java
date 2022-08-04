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
import com.bt.orchestration.ingest.entity.OrderStatus;
import com.bt.orchestration.ingest.entity.Transactions;
import com.bt.orchestration.ingest.entity.WorkflowExecutor;
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
		createDynamicTables(OrderStatus.class, "OrderStatus");

		OrderStatus tracker = OrderStatus.builder().orderId(cartDetails.getCartId())
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

	public void saveWorkflowTracker(List<WorkflowExecutor> workflowList) {
		createDynamicTables(WorkflowExecutor.class, "WorkflowExecutor");
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

}
