package com.bt.orchestration.ingest.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.bt.orchestration.ingest.model.ItemDetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
@Document(collection = "OrderStatus")
public class OrderStatus implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3791224874074298762L;

	@Id
	@Field(name = "CartId")
    private String orderId;
    
	@Field(name = "Status")
    private String status;
    
	@Field(name = "ItemDetails")
    private List<ItemDetails> itemDetails;
    
    public LocalDateTime createdDate;
    
    public LocalDateTime updatedDate;
    
}
