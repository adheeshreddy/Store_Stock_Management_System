package com.stockManagement.models;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseHeader {
	
	private Long id;
	private Long supplierId;
	private Double totalTaxableAmount;
	private Double totalGst;
	private Double totalAmount;
	private LocalDate createdAt;
	private String createdBy;
	private List<PurchaseDetails> purchaseDetails;
}
