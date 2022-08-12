package com.bt.orchestration.ingest.listener;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.bt.orchestration.ingest.exception.OrderIngestException;
import com.bt.orchestration.ingest.service.OrderIngestionService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KafkaMessageListener {
	
	@Autowired
	private OrderIngestionService ingestionService;
	
    @KafkaListener(topics = "execute-workflow", containerFactory = "dynamicKafkaListenerContainerFactory")
    public void processEvent(Map<String,Object> mappedData) throws OrderIngestException {
        log.info("Received Message in group execute-group '{}' and topic execute-workflow", mappedData.get("cartId"));
        ingestionService.saveAndPushToSqs(mappedData);
    }
    
}
