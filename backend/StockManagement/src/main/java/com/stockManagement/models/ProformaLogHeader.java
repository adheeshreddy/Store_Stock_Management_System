package com.stockManagement.models;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProformaLogHeader {
	
	private Long id;
	private Long headerId;
	private Long supplierId;
	private Double totalTaxableAmount;
	private Double totalGst;
	private Double totalAmount;
	private String editType;//edited,deleted
	private LocalDateTime createdAt;
	private String createdBy;
	private List<ProformaLogDetails> proformaLogDetails;
}
