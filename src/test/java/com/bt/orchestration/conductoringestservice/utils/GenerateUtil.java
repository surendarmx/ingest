package com.bt.orchestration.conductoringestservice.utils;

import java.util.HashMap;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;

@Component
public class GenerateUtil {

	private ServiceUtils serviceUtils = new ServiceUtils();
	
	public HashMap<String, Object> getUpstreamData() {
		new TypeReference<HashMap<String,Object>>() {
		};
		return serviceUtils.getMockClass("CartDetails.json", new TypeReference<HashMap<String,Object>>() {
		});
	}

}
