package com.stockManagement.models;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseDetails {
	
	private Long headerId;
	private Long batchId;
	private String productId;
	private Integer quantity;
	private Double taxableAmount;
	private Double gstAmount;
	private Double totalAmount;
	private LocalDate expiry;
	private int reservedQuantity;
	private int availableQuantity;
}
