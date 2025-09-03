package com.stockManagement.models;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockOutHeader {
	 private Long id;     

	    @NotNull(message = "headerId is required")     
	    private Long headerId;

	    @NotNull(message = "supplierId is required")
	    private Long supplierId;
	    
	    @NotNull(message = "status is required")
	    private Character status;

	    @NotNull(message = "totalTaxableAmount is required")
	    @PositiveOrZero
	    private Double totalTaxableAmount;

	    @NotNull(message = "totalGst is required")
	    @PositiveOrZero
	    private Double totalGst;

	    @NotNull(message = "totalAmount is required")
	    @PositiveOrZero
	    private Double totalAmount;

	    @NotNull(message = "approvedAt is required")
	    private LocalDateTime approvedAt;

	    @NotBlank(message = "approvedBy is required")
	    private String approvedBy;

	    @NotEmpty(message = "stockOutDetails cannot be empty")
	    private List<@Valid StockOutDetails> stockOutDetails;
}
