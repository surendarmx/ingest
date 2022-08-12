package com.bt.orchestration.ingest.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.bt.orchestration.ingest.model.CartDetails;
import com.bt.orchestration.ingest.entity.OrderStatus;
import com.bt.orchestration.ingest.entity.Transactions;
import com.bt.orchestration.ingest.entity.WorkflowExecutor;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MongoDBRepository {

	@Autowired
	private MongoTemplate mongoTemplate;

	public void saveOrderStatus(CartDetails cartDetails) {
		OrderStatus tracker = OrderStatus.builder().orderId(cartDetails.getCartId())
				.status(cartDetails.getStatus()).itemDetails(cartDetails.getItemDetails())
				.createdDate(LocalDateTime.now()).build();

		log.info("Saving request to mongo '{}'", tracker);
		mongoTemplate.save(tracker);
		log.info("Saved request to mongo '{}'", tracker);

	}

	public void saveTransaction(Map<String, Object> mappedData) {
		Transactions transaction = Transactions.builder().cartId((String) mappedData.get("cartId"))
				.requestDetails(mappedData).createdDate(LocalDateTime.now()).build();
		log.info("Saving request to mongo DB '{}'", transaction);
		mongoTemplate.save(transaction);
		log.info("Saved request to mongo DB '{}'", transaction);

	}

	public void saveWorkflowTracker(List<WorkflowExecutor> workflowList) {
		mongoTemplate.insertAll(workflowList);
		workflowList.forEach(e -> log.info("Saved order to mongo '{}'", e));
	}

	public boolean isCartIdPresent(String cartId) {
		return !ObjectUtils.isEmpty(getTransactionById(cartId));
	}
	public Transactions getTransactionById(String orderId) {
		log.info("Fetching Transaction record for orderId {}",orderId);
		Query query = new Query();
		query.addCriteria(Criteria.where("CartId").is(orderId));
		List<Transactions> orderStored = mongoTemplate.find(query,Transactions.class);
		
		if (!CollectionUtils.isEmpty(orderStored)) {
			log.info("Transaction record is fetched {}",orderStored);
			return orderStored.get(0);
		}
		log.info("No Transaction record is fetched for the id {}",orderId);
		return null;
	}
}
