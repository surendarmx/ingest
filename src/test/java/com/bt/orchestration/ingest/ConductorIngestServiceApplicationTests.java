package com.bt.orchestration.ingest;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.bt.orchestration.ingest.dao.DynamoDBRepository;
import com.bt.orchestration.ingest.entity.OrderStatus;
import com.bt.orchestration.ingest.entity.Transactions;
import com.bt.orchestration.ingest.entity.WorkflowExecutor;
import com.bt.orchestration.ingest.service.OrderIngestionService;
import com.bt.orchestration.ingest.utils.GenerateUtil;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testcontainers.shaded.org.awaitility.Awaitility.given;

@SpringBootTest
@Testcontainers
@Slf4j
@AutoConfigureMockMvc
public class ConductorIngestServiceApplicationTests {

	private static final String QUEUE_NAME = "workflow-queue";

	@Autowired
	DynamoDBRepository dynamoDbRepo;
	
	@Autowired
	OrderIngestionService ingestionService;
	
	@Autowired
	private AmazonSQS amazonSQS;
	@Autowired
	private DynamoDBMapper dynamoDBMapper;
	@Autowired
	private GenerateUtil generateUtil;

	@Value("${conductor.queue.url}")
	private String conductorQueueUrl;

//	@Container
//	public static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.4.3"));

	@Container
	static LocalStackContainer localStack = new LocalStackContainer(DockerImageName.parse("localstack/localstack"))
			.withServices(LocalStackContainer.Service.SQS, LocalStackContainer.Service.DYNAMODB);

	@BeforeAll
	static void start() throws Exception {
		localStack.execInContainer("awslocal", "sqs", "create-queue", "--queue-name", QUEUE_NAME);
		/*
		 * localStack.execInContainer("awslocal", "dynamodb", "create-table",
		 * "--table-name", "WorkflowTracker", "--billing-mode", "PAY_PER_REQUEST",
		 * "--attribute-definitions", "AttributeName=ItemId,AttributeType=S",
		 * "AttributeName=Status,AttributeType=S",
		 * "AttributeName=Quantity,AttributeType=S", "--key-schema",
		 * "AttributeName=CartId,KeyType=HASH"); localStack.execInContainer("awslocal",
		 * "dynamodb", "create-table", "--table-name", "Transactions", "--billing-mode",
		 * "PAY_PER_REQUEST", "--attribute-definitions",
		 * "AttributeName=CartId,AttributeType=S",
		 * "AttributeName=request,AttributeType=S", "--key-schema",
		 * "AttributeName=TransactionId,KeyType=HASH");
		 */
	}

	@BeforeEach
	public void setup() {
		amazonSQS.purgeQueue(new PurgeQueueRequest(conductorQueueUrl));
	}

	@Test
	public void shouldSaveMessageAndPublishToSqs() {
		// When
		ingestionService.saveAndPushToSqs(generateUtil.getUpstreamData());
		// Then
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
                .withQueueUrl(conductorQueueUrl)
                .withVisibilityTimeout(10)
                .withMaxNumberOfMessages(5);
		List<Message> messages = amazonSQS.receiveMessage(receiveMessageRequest).getMessages();
		assertEquals(2, messages.size());
		PaginatedScanList<Transactions> records = dynamoDBMapper.scan(Transactions.class, new DynamoDBScanExpression());
		assertEquals(1, records.size());
		assertEquals(generateUtil.getUpstreamData().get("cartId"), records.get(0).getCartId());
		PaginatedScanList<OrderStatus> orders = dynamoDBMapper.scan(OrderStatus.class, new DynamoDBScanExpression());
		assertEquals(1, orders.size());
		assertEquals(generateUtil.getUpstreamData().get("cartId"), orders.get(0).getOrderId());
		PaginatedScanList<WorkflowExecutor> workflows = dynamoDBMapper.scan(WorkflowExecutor.class,
				new DynamoDBScanExpression());
		workflows.forEach(e -> log.info("workflows sent to SQS: {}", e));
		assertEquals(2, workflows.size());
		assertEquals(generateUtil.getUpstreamData().get("cartId"), workflows.get(0).getOrderId());
	}

	@DynamicPropertySource
	public static void properties(DynamicPropertyRegistry registry) {
		// Kafka
//		registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);

		// AWS
		URI awsEndpoint = localStack.getEndpointOverride(LocalStackContainer.Service.SQS); // All services are same port
																							// so just get SQS
		registry.add("aws.key", localStack::getAccessKey);
		registry.add("aws.secret", localStack::getSecretKey);
		registry.add("aws.region", localStack::getRegion);
		registry.add("sqs.aws.endpoint", () -> awsEndpoint);
		registry.add("db.aws.endpoint", () -> awsEndpoint);
		registry.add("sqs.conductor.name", () -> QUEUE_NAME);
		registry.add("conductor.queue.url", () -> String.format("%s/000000000000/%s", awsEndpoint, QUEUE_NAME));
	}
}
