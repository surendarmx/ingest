package com.bt.orchestration.ingest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.bt.orchestration.ingest.exception.OrderIngestException;
import com.bt.orchestration.ingest.respoonse.ErrorResponse;

@RestControllerAdvice
public class BaseAdviceController {

	private static final String ORDER_INGESTION_SERVICE = "Order Ingestion Service";

	@ExceptionHandler
	public ResponseEntity<ErrorResponse> handleException(final Exception ex) {

		if (ex instanceof HttpMediaTypeNotSupportedException) {

			return new ResponseEntity<>(getErrorResponse(ex.getMessage()), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
		} else if (ex instanceof HttpMediaTypeNotAcceptableException) {
			return new ResponseEntity<>(getErrorResponse(ex.getMessage()), HttpStatus.NOT_ACCEPTABLE);
		} else if (ex instanceof HttpRequestMethodNotSupportedException) {
			return new ResponseEntity<>(getErrorResponse(ex.getMessage()), HttpStatus.NOT_IMPLEMENTED);
		} else if (ex instanceof HttpMessageNotReadableException) {
			return new ResponseEntity<>(getErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
		} else if (ex instanceof OrderIngestException) {
			OrderIngestException exception = (OrderIngestException) ex;
			return new ResponseEntity<>(getErrorResponse(exception.getSystemMessage()), exception.getHttpStatus());
		} else {
			return new ResponseEntity<>(getErrorResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private ErrorResponse getErrorResponse(String systemMessage) {
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setSystem(ORDER_INGESTION_SERVICE);
		errorResponse.setSystemMessage(systemMessage);
		return errorResponse;
	}

}
