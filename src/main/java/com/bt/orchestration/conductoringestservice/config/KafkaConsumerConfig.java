package com.bt.orchestration.conductoringestservice.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.bt.orchestration.conductoringestservice.model.CartDetails;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "execute-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    
    @Bean
    public ConsumerFactory<String, CartDetails> cartDetailsConsumerFactory() {
    	 Map<String, Object> props = new HashMap<>();
         props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
         props.put(ConsumerConfig.GROUP_ID_CONFIG, "execute-group");
         props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
         props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
         props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(
          props,
          new StringDeserializer(), 
          new JsonDeserializer<>(CartDetails.class));
    }
    
    @Bean
    public ConsumerFactory<String, Map<String,Object>> dynamicConsumerFactory() {
    	 Map<String, Object> props = new HashMap<>();
         props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
         props.put(ConsumerConfig.GROUP_ID_CONFIG, "execute-group");
         props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
         props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
         props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(
          props,
          new StringDeserializer(), 
          new JsonDeserializer<>(Map.class));
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CartDetails> cartDetailsKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CartDetails> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cartDetailsConsumerFactory());
        return factory;
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Map<String,Object>> dynamicKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Map<String,Object>> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(dynamicConsumerFactory());
        return factory;
    }
}