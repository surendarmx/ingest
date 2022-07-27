package com.bt.orchestration.conductoringestservice.dao;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ListMapConverter implements DynamoDBTypeConverter<String, Map<String, Object>> {
	@Override
	public String convert(Map<String, Object> mapList) {
		try {
			if (mapList != null) {
				ObjectMapper mapper = new ObjectMapper();
				return mapper.writeValueAsString(mapList);
			} else {
				throw new Exception("map is empty");
			}
		} catch (Exception e) {
			log.error(String.format("Error converting map to Dynamo String. Reason - {%s}", e.getMessage()));
			return "";
		}
	}

	@Override
	public Map<String, Object> unconvert(String str) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(str, new TypeReference<Map<String, Object>>() {
			});
		} catch (Exception e) {
			log.error(String.format("Error unconverting Dynamo String to map. Reason - {%s}", e.getMessage()));
			return new HashMap<>();
		}
	}
}
