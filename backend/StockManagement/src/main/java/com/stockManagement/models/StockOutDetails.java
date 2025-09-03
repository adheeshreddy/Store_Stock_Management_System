package com.stockManagement.models;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockOutDetails {
	private Long stockOutId;  
	private Long headerId;
	private Long batchId;
	private String productId;
	private Integer quantity;
//	private String type;//Invoice/return
	private Double taxableAmount;
	private Double gstAmount;
	private Double totalAmount;
	private LocalDate expiry;
}
