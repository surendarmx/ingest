package com.bt.orchestration.ingest.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
@Document(collection = "WorkflowExecutor")
public class WorkflowExecutor implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -488827080789691314L;

	@Id
	@Field(name = "TrackerId")
    private String trackerId;
	
	@Field(name = "CartId")
    private String orderId;
    
	@Field(name = "ItemId")
    private String itemId;

	@Field(name = "Status")
    private String eventStatus;
    
	@Field(name = "Quantity")
    private String quantity;
    
    public LocalDateTime createdDate;
}
