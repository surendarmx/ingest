package com.bt.orchestration.ingest.listener;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.bt.orchestration.ingest.dao.DynamoDBRepository;
import com.bt.orchestration.ingest.service.OrderIngestionService;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KafkaMessageListener {
	
	@Autowired
	private OrderIngestionService ingestionService;
	
    @KafkaListener(topics = "provision", containerFactory = "dynamicKafkaListenerContainerFactory")
    public void processEvent(Map<String,Object> mappedData) throws JsonProcessingException {
        log.info("Received Message in group execute-group '{}' and topic provision", mappedData.get("cartId"));
        ingestionService.saveAndPushToSqs(mappedData);
    }
    
}
