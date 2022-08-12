package com.bt.orchestration.ingest.utils;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.bt.orchestration.ingest.dao.MongoDBRepository;
import com.bt.orchestration.ingest.exception.OrderIngestException;
import com.bt.orchestration.ingest.model.ItemDetails;

@Component
public class ValidateUtil {

	@Autowired
	MongoDBRepository mongoDbRepo;

	public void validateCartId(Object cartId) throws OrderIngestException {
		if (ObjectUtils.isEmpty(cartId)) {
			throw new OrderIngestException("Cart ID is not provided in the request", HttpStatus.BAD_REQUEST);
		} else if (mongoDbRepo.isCartIdPresent((String) cartId)) {
			throw new OrderIngestException("Cart ID is already present", HttpStatus.BAD_REQUEST);
		}
	}

	public void validateItemDetails(List<ItemDetails> itemDetails) throws OrderIngestException {
		if (CollectionUtils.isEmpty(itemDetails)) {
			throw new OrderIngestException("orderItems is not provided in the request", HttpStatus.BAD_REQUEST);
		}
		for (ItemDetails item : itemDetails) {
			if (!StringUtils.hasText(item.getProductId())) {
				throw new OrderIngestException("ProductId is not provided in the request", HttpStatus.BAD_REQUEST);
			}
		}
	}

}
