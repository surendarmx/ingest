package com.bt.orchestration.ingest.model;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class CartDetails {

	private String cartId;
	private String event;
	private String status;
	private List<ItemDetails> itemDetails; 
	private LocalDateTime createdDate;
}
