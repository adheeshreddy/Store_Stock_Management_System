package com.stockManagement.models;

import java.time.LocalDate;

import lombok.Data;

@Data
public class ProformaLogDetails {
	
	private Long logId;
	private Long headerId;
	private String batchId;
	private String productId;
	private Integer quantity;
	private Double taxableAmount;
	private Double gstAmount;
	private Double totalAmount;
	private LocalDate expiry;
	private String status;//edited,deleted
}
