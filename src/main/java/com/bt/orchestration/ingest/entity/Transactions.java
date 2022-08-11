package com.bt.orchestration.ingest.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

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
@Document(collection = "Transactions")
public class Transactions implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = -5743459299653149245L;

	@Id
	@Field(name = "TransactionId")
    private String transactionId;
    
	@Field(name = "CartId")
    private String cartId;

	@Field(name = "request")
    private Map<String,Object> requestDetails;
    
    public LocalDateTime createdDate;
    
}
