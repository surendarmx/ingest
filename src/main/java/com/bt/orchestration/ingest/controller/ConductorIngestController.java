package com.bt.orchestration.ingest.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.bt.orchestration.ingest.service.OrderIngestionService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class ConductorIngestController {
	
	@Autowired
	private OrderIngestionService ingestionService;

	@Value("${kafka.execute-workflow.topic-name}")
	private String executeWorkflowTopic;
	
	/*
	 * @Autowired private AmazonSQS amazonSQS;
	 */
	
	/*
	 * @GetMapping("/sqs/read") public ResponseEntity<List<String>>
	 * consumeSqsQueueMessages() { AtomicInteger counter = new AtomicInteger(1);
	 * 
	 * CreateQueueRequest conductorQueueRequest = new
	 * CreateQueueRequest("workflow-queue"); String conductorQueueUrl =
	 * amazonSQS.createQueue(conductorQueueRequest) .getQueueUrl();
	 * ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
	 * .withQueueUrl(conductorQueueUrl) .withVisibilityTimeout(10)
	 * .withMaxNumberOfMessages(5); List<Message> pushedMessages =
	 * amazonSQS.receiveMessage(receiveMessageRequest).getMessages(); List<String>
	 * messages = new ArrayList<>(); pushedMessages.stream().forEach( e ->
	 * messages.add("Message(" + counter.getAndIncrement() + ") stored in SQS: {" +
	 * e.getBody() + "}")); return ResponseEntity.ok(messages); }
	 */
	


	@PostMapping("/order")
	public ResponseEntity<String> pushOrderMessage(@RequestHeader HttpHeaders headers,
			@RequestBody Map<String, Object> mappedData) {
		ingestionService.pushToKafkaTopic(mappedData,executeWorkflowTopic);
		return ResponseEntity.ok().build();
	}
}
