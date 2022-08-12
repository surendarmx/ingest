package com.bt.orchestration.ingest.exception;

import org.springframework.http.HttpStatus;

public class OrderIngestException extends Exception {
	private static final long serialVersionUID = 1L;
	private final HttpStatus httpStatus;
	private final String systemMessage;

	public OrderIngestException(String systemMessage, HttpStatus httpStatus) {
		this.systemMessage = systemMessage;
		this.httpStatus = httpStatus;
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	public String getSystemMessage() {
		return systemMessage;
	}

}
