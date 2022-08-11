package com.bt.orchestration.ingest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;

@Configuration
public class AwsConfig {

    @Value("${sqs.aws.endpoint}")
    private String sqsEndpoint;

    @Value("${aws.key}")
    private String key;

    @Value("${aws.secret}")
    private String secret;

    @Value("${aws.region}")
    private String region;

    @Bean
    public AmazonSQS amazonSQSAsync() {
        return AmazonSQSAsyncClientBuilder
                .standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(sqsEndpoint, region))
                .withCredentials(getCredentialsProvider())
                .build();
    }
    
//    @Bean
//    public QueueMessagingTemplate queueMessagingTemplate(
//      AmazonSQSAsync amazonSQSAsync) {
//        return new QueueMessagingTemplate(amazonSQSAsync);
//    }
    

//    @Bean
//    public AwsClientBuilder.EndpointConfiguration endpointConfiguration() {
//        return new AwsClientBuilder.EndpointConfiguration(dbEndpoint, region);
//    }

    @Bean
    public AWSStaticCredentialsProvider getCredentialsProvider() {
        return new AWSStaticCredentialsProvider(new BasicAWSCredentials(key, secret));
    }
    
}
