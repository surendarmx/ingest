package com.bt.orchestration.conductoringestservice.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.bt.orchestration.conductoringestservice.model.CartDetails;
import com.bt.orchestration.conductoringestservice.model.ItemDetails;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class ConductorIngestController {
	
	@Autowired
	private AmazonSQS amazonSQS;

	@Value("${kafka.provision.topic-name}")
	private String provisionTopic;
	
	@GetMapping("/sqs/read")
	public ResponseEntity<List<String>> consumeSqsQueueMessages() {
		AtomicInteger counter = new AtomicInteger(1);
		
		CreateQueueRequest conductorQueueRequest = new CreateQueueRequest("workflow-queue");
		String conductorQueueUrl = amazonSQS.createQueue(conductorQueueRequest)
	            .getQueueUrl();
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
                .withQueueUrl(conductorQueueUrl)
                .withVisibilityTimeout(10)
                .withMaxNumberOfMessages(5);
		List<Message> pushedMessages = amazonSQS.receiveMessage(receiveMessageRequest).getMessages();
		List<String> messages = new ArrayList<>();
		pushedMessages.stream().forEach(
				e -> messages.add("Message(" + counter.getAndIncrement() + ") stored in SQS: {" + e.getBody() + "}"));
		return ResponseEntity.ok(messages);
	}
	
	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;
	
	@Autowired
	private KafkaTemplate<String, Map<String,Object>> dynamicKafkaTemplate;

	@PostMapping("/message")
	public ResponseEntity<String> pushKafkaMessage(@RequestHeader HttpHeaders headers,
			@RequestBody Map<String, String> mappedData) {
		ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(provisionTopic, mappedData.get("message"));

		future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {

			@Override
			public void onSuccess(SendResult<String, String> result) {
				log.info(
						"Sent message=[" + mappedData.get("message") + "] with offset=[" + result.getRecordMetadata().offset() + "]");
			}

			@Override
			public void onFailure(Throwable ex) {
				log.info("Unable to send message=[" + mappedData.get("message") + "] due to : " + ex.getMessage());
			}
		});
		return ResponseEntity.ok().build();
	}
	
	@PostMapping("/order")
	public ResponseEntity<String> pushOrderMessage(@RequestHeader HttpHeaders headers,
			@RequestBody Map<String, Object> mappedData) {
		
		ListenableFuture<SendResult<String, Map<String, Object>>> future = dynamicKafkaTemplate.send(provisionTopic, mappedData);

		future.addCallback(new ListenableFutureCallback<SendResult<String, Map<String, Object>>>() {

			@Override
			public void onSuccess(SendResult<String, Map<String, Object>> result) {
				log.info(
						"Sent message=[" + mappedData.get("cartId") + "] with offset=[" + result.getRecordMetadata().offset() + "]");
			}

			@Override
			public void onFailure(Throwable ex) {
				log.info("Unable to send message=[" + mappedData.get("cartId") + "] due to : " + ex.getMessage());
			}
		});
		return ResponseEntity.ok().build();
	}
}
