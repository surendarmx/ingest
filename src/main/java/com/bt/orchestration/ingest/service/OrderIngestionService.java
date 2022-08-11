package com.bt.orchestration.ingest.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.bt.orchestration.ingest.dao.MongoDBRepository;
import com.bt.orchestration.ingest.model.CartDetails;
import com.bt.orchestration.ingest.model.ItemDetails;
import com.bt.orchestration.ingest.entity.WorkflowExecutor;
import com.bt.orchestration.ingest.sqs.SQSMessageForwarder;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderIngestionService {

	@Autowired
	private KafkaTemplate<String, Map<String, Object>> dynamicKafkaTemplate;
	
	@Autowired
	private MongoDBRepository mongoDbRepo;

	@Autowired
	SQSMessageForwarder sqsMessageForwarder;

	@Value("${sqs.conductor.name}")
	private String sqsWorkflowQueueName;
	
	public void pushToKafkaTopic(Map<String, Object> mappedData, String provisionTopic) {

		ListenableFuture<SendResult<String, Map<String, Object>>> future = dynamicKafkaTemplate.send(provisionTopic,
				mappedData);

		future.addCallback(new ListenableFutureCallback<SendResult<String, Map<String, Object>>>() {

			@Override
			public void onSuccess(SendResult<String, Map<String, Object>> result) {
				log.info("Sent message=[" + mappedData.get("cartId") + "] with offset=["
						+ result.getRecordMetadata().offset() + "]");
			}

			@Override
			public void onFailure(Throwable ex) {
				log.info("Unable to send message=[" + mappedData.get("cartId") + "] due to : " + ex.getMessage());
			}
		});

	}

	public void saveAndPushToSqs(Map<String, Object> mappedData) {

		mongoDbRepo.saveTransaction(mappedData);
		
		CartDetails cartDetails = mapCartDetails(mappedData);
		mongoDbRepo.saveOrderStatus(cartDetails);
		
		List<WorkflowExecutor> workflowList = new ArrayList<>();
		cartDetails.getItemDetails().forEach(e -> {

			WorkflowExecutor tracker = WorkflowExecutor.builder().orderId(cartDetails.getCartId())
					.itemId(e.getProductId()).quantity(e.getQuantity()).eventStatus(cartDetails.getEvent())
					.createdDate(LocalDateTime.now()).build();
			workflowList.add(tracker);
			log.info("Saving order to mongo '{}'", tracker);
			
			try {
				sqsMessageForwarder.pushMessage(tracker, sqsWorkflowQueueName);
			} catch (JsonProcessingException e1) {
				log.error("Error occured due to '{}'", e1.getMessage());
			}
		});
		
		mongoDbRepo.saveWorkflowTracker(workflowList);
	}

	public CartDetails mapCartDetails(Map<String, Object> mappedData) {
		CartDetails cartDetails = new CartDetails();
		cartDetails.setCartId((String) mappedData.get("cartId"));
		cartDetails.setEvent((String) mappedData.get("eventName"));
		cartDetails.setStatus("IN_PROGRESS");
		List<ItemDetails> itemList = new ArrayList<>();
		cartDetails.setItemDetails(itemList);
		if (mappedData.get("orderItems") != null) {
			List<Map<String, Object>> mappedItems = (List<Map<String, Object>>) mappedData.get("orderItems");
			mappedItems.stream().forEach(e -> {
				ItemDetails item = new ItemDetails();
				item.setProductId((String) e.get("productId"));
				item.setQuantity(String.valueOf(e.get("quantity")));
				item.setStatus("PENDING");
				itemList.add(item);
			});

		}
		return cartDetails;
	}

}
