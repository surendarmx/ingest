package com.bt.orchestration.conductoringestservice.utils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServiceUtils {

	public <T> T getMockClass(final String fileName, final Class<T> actualClass) {

		T returnClass = null;
		try {
			String jsonStringMockResponse = new String(
					Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(fileName).toURI())));
			returnClass = getObjectMapper().readValue(jsonStringMockResponse, actualClass);

		} catch (Exception e) {
			log.error("Exception occured while mocking the data", e);
		}
		return returnClass;
	}
	
	public <T> T getMockClass(String fileName, TypeReference<T> typeReference) {

		T returnClass = null;
		try {
			String jsonStringMockResponse = new String(
					Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(fileName).toURI())));
			returnClass = getObjectMapper().readValue(jsonStringMockResponse, typeReference);

		} catch (Exception e) {
			log.error("Exception occured while mocking the data", e);
		}
		return returnClass;
	}
	

	private ObjectMapper getObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setDefaultPropertyInclusion(JsonInclude.Value.construct(Include.NON_NULL, Include.NON_NULL));
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
		return objectMapper;
	}

	public String getMockResponseAsString(final String fileName) {
		String jsonStringMockResponse = null;
		try {
			jsonStringMockResponse = new String(
					Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(fileName).toURI())));

		} catch (Exception e) {
			log.error("Exception occured while mocking the data", e);
		}
		return jsonStringMockResponse;
	}

}
