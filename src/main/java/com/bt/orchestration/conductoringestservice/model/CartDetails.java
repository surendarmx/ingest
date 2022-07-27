package com.bt.orchestration.conductoringestservice.model;

import java.util.List;

import lombok.Data;

@Data
public class CartDetails {

	private String cartId;
	private String event;
	private List<ItemDetails> itemDetails; 
}
