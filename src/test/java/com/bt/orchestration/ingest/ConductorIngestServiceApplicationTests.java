package com.bt.orchestration.ingest;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.bt.orchestration.ingest.config.KafkaConsumerConfig;
import com.bt.orchestration.ingest.dao.MongoDBRepository;
import com.bt.orchestration.ingest.entity.OrderStatus;
import com.bt.orchestration.ingest.entity.Transactions;
import com.bt.orchestration.ingest.entity.WorkflowExecutor;
import com.bt.orchestration.ingest.exception.OrderIngestException;
import com.bt.orchestration.ingest.model.CartDetails;
import com.bt.orchestration.ingest.service.OrderIngestionService;
import com.bt.orchestration.ingest.utils.GenerateUtil;
import com.bt.orchestration.ingest.utils.ValidateUtil;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testcontainers.shaded.org.awaitility.Awaitility.given;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest
@Testcontainers
@Slf4j
@AutoConfigureMockMvc
public class ConductorIngestServiceApplicationTests {

	private static final String QUEUE_NAME = "workflow-queue";

	@Autowired
	MongoTemplate mongoTemplate;
	
	@Autowired
	OrderIngestionService ingestionService;
	
	@Autowired
	private AmazonSQS amazonSQS;

	@Autowired
	private GenerateUtil generateUtil;

	@Autowired
	private ValidateUtil validateUtil;

	/**
     * Countdown latch
     */
    private CountDownLatch lock = new CountDownLatch(1);

	@Autowired
	private KafkaConsumerConfig consumer;
	
	@Value("${conductor.queue.url}")
	private String conductorQueueUrl;
	
	@Value("${kafka.execute-workflow.topic-name}")
	private String kafkaWorkflowTopic;
    
	@Container
	public static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.4.3"));

	@Container
	static LocalStackContainer localStack = new LocalStackContainer(DockerImageName.parse("localstack/localstack"))
			.withServices(LocalStackContainer.Service.SQS);
	@Container
	public static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo"));

	@BeforeAll
	static void start() throws Exception {
		localStack.execInContainer("awslocal", "sqs", "create-queue", "--queue-name", QUEUE_NAME);
	}

	@BeforeEach
	public void setup() {
		amazonSQS.purgeQueue(new PurgeQueueRequest(conductorQueueUrl));
		mongoTemplate.dropCollection(Transactions.class);
		mongoTemplate.dropCollection(OrderStatus.class);
		mongoTemplate.dropCollection(WorkflowExecutor.class);
	}
	/**
	 * Integration Tests
	 * @throws InterruptedException 
	 */
	@Test
	void shouldConsumeKafkaMessages() throws InterruptedException{
		// When
		ingestionService.pushToKafkaTopic(generateUtil.getUpstreamData(), kafkaWorkflowTopic);
		lock.await(5, TimeUnit.SECONDS);
		// Then
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
                .withQueueUrl(conductorQueueUrl)
                .withVisibilityTimeout(10)
                .withMaxNumberOfMessages(5);
		List<Message> messages = amazonSQS.receiveMessage(receiveMessageRequest).getMessages();
		// Testing SQS message count
		assertEquals(2, messages.size());
		
		//Testing Mongo DB data 
		List<Transactions> records = mongoTemplate.findAll(Transactions.class);
		assertEquals(1, records.size());
		assertEquals(generateUtil.getUpstreamData().get("cartId"), records.get(0).getCartId());
		List<OrderStatus> orders = mongoTemplate.findAll(OrderStatus.class);
		assertEquals(1, orders.size());
		assertEquals(generateUtil.getUpstreamData().get("cartId"), orders.get(0).getOrderId());
		List<WorkflowExecutor> workflows = mongoTemplate.findAll(WorkflowExecutor.class);
		workflows.forEach(e -> log.info("workflows sent to SQS: {}", e));
		assertEquals(2, workflows.size());
		//Checking cartId
		assertEquals(generateUtil.getUpstreamData().get("cartId"), workflows.get(0).getOrderId());
	}
	
//	@Test
//	void shouldSaveMessageAndPublishToSqs() throws OrderIngestException {
//		// When
//		ingestionService.saveAndPushToSqs(generateUtil.getUpstreamData());
//		// Then
//		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
//                .withQueueUrl(conductorQueueUrl)
//                .withVisibilityTimeout(10)
//                .withMaxNumberOfMessages(5);
//		List<Message> messages = amazonSQS.receiveMessage(receiveMessageRequest).getMessages();
//		// Testing SQS message count
//		assertEquals(2, messages.size());
//		
//		//Testing Mongo DB data 
//		List<Transactions> records = mongoTemplate.findAll(Transactions.class);
//		assertEquals(1, records.size());
//		assertEquals(generateUtil.getUpstreamData().get("cartId"), records.get(0).getCartId());
//		List<OrderStatus> orders = mongoTemplate.findAll(OrderStatus.class);
//		assertEquals(1, orders.size());
//		assertEquals(generateUtil.getUpstreamData().get("cartId"), orders.get(0).getOrderId());
//		List<WorkflowExecutor> workflows = mongoTemplate.findAll(WorkflowExecutor.class);
//		workflows.forEach(e -> log.info("workflows sent to SQS: {}", e));
//		assertEquals(2, workflows.size());
//		//Checking cartId
//		assertEquals(generateUtil.getUpstreamData().get("cartId"), workflows.get(0).getOrderId());
//	}
	/**
	 * Unit Tests
	 */
	@Test
	void validateEmptyCartId() {
		Exception exception = assertThrows(Exception.class, () -> {
			// When
			validateUtil.validateCartId("");
		});
		OrderIngestException ex = (OrderIngestException) exception;
		String expectedMessage = "Cart ID is not provided in the request";

		assertEquals(expectedMessage,ex.getSystemMessage());

	}
	@Test
	void validateAlreadyPresentCartId() throws OrderIngestException {
		//PreRequisite
		ingestionService.saveAndPushToSqs(generateUtil.getUpstreamData());
		Exception exception = assertThrows(Exception.class, () -> {
			// When
			validateUtil.validateCartId(generateUtil.getUpstreamData().get("cartId"));
		});
		OrderIngestException ex = (OrderIngestException) exception;
		String expectedMessage = "Cart ID is already present";

		assertEquals(expectedMessage,ex.getSystemMessage());

	}
	
	@Test
	void validateEmptyItemDetails() {
		Exception exception = assertThrows(Exception.class, () -> {
			// When
			validateUtil.validateItemDetails(new ArrayList<>());
		});
		OrderIngestException ex = (OrderIngestException) exception;
		String expectedMessage = "orderItems is not provided in the request";

		assertEquals(expectedMessage,ex.getSystemMessage());

	}
	
	@Test
	void mapCartDetails() throws OrderIngestException {
		//When
		CartDetails details = ingestionService.mapCartDetails(generateUtil.getUpstreamData());
		
		assertEquals(generateUtil.getUpstreamData().get("cartId"),details.getCartId());

	}
	
	@Test
	void validateEmptyProductId() throws OrderIngestException {
		//PreRequisite
		CartDetails details = ingestionService.mapCartDetails(generateUtil.getUpstreamData());
		details.getItemDetails().get(0).setProductId(null);		
		Exception exception = assertThrows(Exception.class, () -> {
			// When
			validateUtil.validateItemDetails(details.getItemDetails());
		});
		OrderIngestException ex = (OrderIngestException) exception;
		String expectedMessage = "ProductId is not provided in the request";

		assertEquals(expectedMessage,ex.getSystemMessage());

	}
	
	/**
	 * Setting properties
	 * @param registry
	 */
	@DynamicPropertySource
	public static void properties(DynamicPropertyRegistry registry) {
		// Kafka
		registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);

		// AWS
		URI awsEndpoint = localStack.getEndpointOverride(LocalStackContainer.Service.SQS); // All services are same port
		registry.add("aws.key", localStack::getAccessKey);
		registry.add("aws.secret", localStack::getSecretKey);
		registry.add("aws.region", localStack::getRegion);
		//SQS
		registry.add("sqs.aws.endpoint", () -> awsEndpoint);
		registry.add("sqs.conductor.name", () -> QUEUE_NAME);
		//SQS Conductor Queue
		registry.add("conductor.queue.url", () -> String.format("%s/000000000000/%s", awsEndpoint, QUEUE_NAME));
		//Mongo DB
		registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}
}
