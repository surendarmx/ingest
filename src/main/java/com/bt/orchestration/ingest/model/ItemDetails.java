package com.bt.orchestration.ingest.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class ItemDetails implements Serializable{
	private String productId;
	private String quantity;
	private String status;
}
