package com.bt.orchestration.conductoringestservice.config;

import java.util.HashMap;
import java.util.Map;


import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.bt.orchestration.conductoringestservice.model.CartDetails;

@Configuration
@EnableKafka
public class KafkaProducerConfig{

	@Value("${spring.kafka.bootstrap-servers}")
	private String kafkaBrokerList;

	@Bean("defaultExecutor")
	public ThreadPoolTaskExecutor defaultExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(1);
		executor.setAllowCoreThreadTimeOut(false);
		executor.setMaxPoolSize(2);
		executor.setKeepAliveSeconds(1);
		executor.setQueueCapacity(5);
		executor.setWaitForTasksToCompleteOnShutdown(false);
		return executor;
	}

	@Bean
	public ProducerFactory<String, String> producerFactory() {
		return new DefaultKafkaProducerFactory<>(producerConfigs());
	}

	@Bean
	public Map<String, Object> producerConfigs() {
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokerList);
		props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		props.put(ProducerConfig.RETRIES_CONFIG, 0);
		props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
		props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
		props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
		return props;
	}

	@Bean
	public KafkaTemplate<String, String> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}
	
	@Bean
	public ProducerFactory<String, CartDetails> cartDetailsProducerFactory() {
		Map<String,Object> configProps = producerConfigs();
	    configProps.put(
	    	      ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, 
	    	      JsonSerializer.class);
		return new DefaultKafkaProducerFactory<>(configProps);
	}
	
	@Bean
	public KafkaTemplate<String, CartDetails> cartDetailsKafkaTemplate() {
		return new KafkaTemplate<>(cartDetailsProducerFactory());
	}
	
	@Bean
	public ProducerFactory<String, Map<String,Object>> dynamicProducerFactory() {
		Map<String,Object> configProps = producerConfigs();
	    configProps.put(
	    	      ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, 
	    	      JsonSerializer.class);
		return new DefaultKafkaProducerFactory<>(configProps);
	}
	
	@Bean
	public KafkaTemplate<String, Map<String,Object>> dynamicKafkaTemplate() {
		return new KafkaTemplate<>(dynamicProducerFactory());
	}
}
