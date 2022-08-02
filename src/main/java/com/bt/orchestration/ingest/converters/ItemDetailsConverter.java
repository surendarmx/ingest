package com.bt.orchestration.ingest.converters;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.bt.orchestration.ingest.model.ItemDetails;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ItemDetailsConverter implements DynamoDBTypeConverter<String, List<ItemDetails>> {
	@Override
	public String convert(List<ItemDetails> itemDetails) {
		try {
			if (!CollectionUtils.isEmpty(itemDetails)) {
				ObjectMapper mapper = new ObjectMapper();
				return mapper.writeValueAsString(itemDetails);
			} else {
				throw new Exception("itemDetails is empty");
			}
		} catch (Exception e) {
			log.error(String.format("Error converting map to Dynamo String. Reason - {%s}", e.getMessage()));
			return "";
		}
	}

	@Override
	public List<ItemDetails> unconvert(String str) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(str, new TypeReference<List<ItemDetails>>() {
			});
		} catch (Exception e) {
			log.error(String.format("Error unconverting Dynamo String to itemDetails. Reason - {%s}", e.getMessage()));
			return new ArrayList<>();
		}
	}
}
