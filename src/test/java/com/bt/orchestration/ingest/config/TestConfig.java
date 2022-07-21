package com.bt.orchestration.ingest.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@TestConfiguration
public class TestConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${aws.endpoint}")
    private String endpoint;

    @Value("${aws.key}")
    private String key;

    @Value("${aws.secret}")
    private String secret;

    @Value("${aws.region}")
    private String region;

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    @Primary
    public AmazonSQS localSqs() {
        return AmazonSQSAsyncClientBuilder
                .standard()
                .withEndpointConfiguration(endpointConfiguration())
                .withCredentials(getCredentialsProvider())
                .build();
    }

    @Bean
    @Primary
    public AmazonDynamoDB localDynamoDB() {
        return AmazonDynamoDBClientBuilder.standard()
                .withCredentials(getCredentialsProvider())
                .withEndpointConfiguration(endpointConfiguration())
                .build();
    }

    @Bean
    public AwsClientBuilder.EndpointConfiguration endpointConfiguration() {
        return new AwsClientBuilder.EndpointConfiguration(endpoint, region);
    }

    @Bean
    public AWSStaticCredentialsProvider getCredentialsProvider() {
        return new AWSStaticCredentialsProvider(new BasicAWSCredentials(key, secret));
    }
}