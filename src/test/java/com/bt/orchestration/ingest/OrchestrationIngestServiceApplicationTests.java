package com.bt.orchestration.ingest;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.bt.orchestration.ingest.config.TestConfig;
import com.bt.orchestration.ingest.model.OrderStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testcontainers.shaded.org.awaitility.Awaitility.given;

@SpringBootTest
@Testcontainers
@Import(TestConfig.class)
public class OrchestrationIngestServiceApplicationTests {

//    private static final String QUEUE_NAME = "orchestration-queue";
//
//    @Autowired
//    private KafkaTemplate<String, String> kafkaTemplate;
//    @Autowired
//    private AmazonSQS amazonSQS;
//    @Autowired
//    private DynamoDBMapper dynamoDBMapper;
//
//    @Value("${conductor.queue.url}")
//    private String conductorQueueUrl;
//
//    @Container
//    public static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.4.3"));
//
//    @Container
//    static LocalStackContainer localStack = new LocalStackContainer(DockerImageName.parse("localstack/localstack"))
//                    .withServices(LocalStackContainer.Service.SQS, LocalStackContainer.Service.DYNAMODB);
//
//    @BeforeAll
//    static void start() throws Exception {
//        localStack.execInContainer("awslocal", "sqs", "create-queue", "--queue-name", QUEUE_NAME);
//        localStack.execInContainer("awslocal", "dynamodb", "create-table",
//                "--table-name", "OrderStatus",
//                "--billing-mode", "PAY_PER_REQUEST",
//                "--attribute-definitions", "AttributeName=OrderId,AttributeType=S",
//                "--key-schema", "AttributeName=OrderId,KeyType=HASH");
//    }
//
//    @BeforeEach
//    public void setup() {
//        amazonSQS.purgeQueue(new PurgeQueueRequest(conductorQueueUrl));
//    }
//
//    @Test
//    public void shouldSaveMessageAndPublishToSqs() {
//        // Given
//        String message = "My kafka message";
//
//        // When
//        kafkaTemplate.send("execute-workflow", message);
//
//        // Then
//        given()
//                .await()
//                .atMost(5, TimeUnit.SECONDS)
//                .untilAsserted(() -> {
//                    List<Message> messages = amazonSQS.receiveMessage(conductorQueueUrl).getMessages();
//                    assertEquals(1, messages.size());
//                    assertEquals(message, messages.get(0).getBody());
//                });
//        PaginatedScanList<OrderStatus> records = dynamoDBMapper.scan(OrderStatus.class, new DynamoDBScanExpression());
//        assertEquals(1, records.size());
//        assertEquals(message, records.get(0).getMessage());
//    }
//
//    @DynamicPropertySource
//    public static void properties(DynamicPropertyRegistry registry) {
//        // Kafka
//        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
//
//        // AWS
//        URI awsEndpoint = localStack.getEndpointOverride(LocalStackContainer.Service.SQS); // All services are same port so just get SQS
//        registry.add("aws.key", localStack::getAccessKey);
//        registry.add("aws.secret", localStack::getSecretKey);
//        registry.add("aws.region", localStack::getRegion);
//        registry.add("aws.endpoint", () -> awsEndpoint);
//        registry.add("conductor.queue.url", () -> String.format("%s/000000000000/%s", awsEndpoint, QUEUE_NAME));
//    }
}
