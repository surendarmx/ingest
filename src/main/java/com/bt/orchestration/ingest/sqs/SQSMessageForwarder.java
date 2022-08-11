package com.bt.orchestration.ingest.sqs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SQSMessageForwarder {
	
	@Autowired
	private AmazonSQS amazonSQS;
	
	@Autowired
	private ObjectMapper objMapper;

	public void pushMessage(Object messageObject, String queueName) throws JsonProcessingException {
		CreateQueueRequest conductorQueueRequest = new CreateQueueRequest(queueName);
		String conductorQueueUrl = amazonSQS.createQueue(conductorQueueRequest).getQueueUrl();
		log.info("Sending message to the queue '{}'", conductorQueueUrl);
		String serializedJson = objMapper.writeValueAsString(messageObject);
		log.info("Converted message to be sent to the queue '{}'", serializedJson);
		SendMessageRequest sendMsgRequest = new SendMessageRequest().withQueueUrl(conductorQueueUrl).withMessageBody(serializedJson);
		log.info("Sent message to SQS {}", amazonSQS.sendMessage(sendMsgRequest));

	}
	
}
