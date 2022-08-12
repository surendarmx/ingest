package com.bt.orchestration.ingest.respoonse;

import lombok.Data;

@Data
public class ErrorResponse {
	
	private String systemMessage;
	private String system;

}
