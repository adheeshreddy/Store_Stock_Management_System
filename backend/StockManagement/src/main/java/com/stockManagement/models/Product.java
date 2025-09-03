package com.stockManagement.models;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product {

	@NotEmpty(message = "Product Id cannot be empty")
	private String id;

	@NotEmpty(message = "Product Name cannot be empty")
	@Size(min = 3, message = "Product Name cannot be less than 3 characters")
	private String name;

	@NotEmpty(message = "Status cannot be empty")
	private String status;

}
