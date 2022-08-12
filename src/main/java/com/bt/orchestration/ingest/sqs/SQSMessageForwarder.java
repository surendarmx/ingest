package com.bt.orchestration.ingest.sqs;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.bt.orchestration.ingest.entity.WorkflowExecutor;
import com.bt.orchestration.ingest.exception.OrderIngestException;
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
	
	@Value("${sqs.conductor.name}")
	private String sqsWorkflowQueueName;
	
	public void pushMessage(Object messageObject, String queueName) throws OrderIngestException {
		CreateQueueRequest conductorQueueRequest = new CreateQueueRequest(queueName);
		String conductorQueueUrl = amazonSQS.createQueue(conductorQueueRequest).getQueueUrl();
		log.info("Sending message to the queue '{}'", conductorQueueUrl);
		try {
			String serializedJson = objMapper.writeValueAsString(messageObject);
			log.info("Converted message to be sent to the queue '{}'", serializedJson);
			SendMessageRequest sendMsgRequest = new SendMessageRequest().withQueueUrl(conductorQueueUrl).withMessageBody(serializedJson);
			log.info("Sent message to SQS {}", amazonSQS.sendMessage(sendMsgRequest));
		}
		catch(Exception e) {
			log.error("Error occured due to {}", e.getMessage());
			throw new OrderIngestException("JsonProcessingException occured due to "+e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	public void pushWorkflowMessages(List<WorkflowExecutor> workflowList) throws OrderIngestException {
		for (WorkflowExecutor workflowObj : workflowList) {
			pushMessage(workflowObj, sqsWorkflowQueueName);
		}
	}
	
}
