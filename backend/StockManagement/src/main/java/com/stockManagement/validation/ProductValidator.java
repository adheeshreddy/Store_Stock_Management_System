package com.stockManagement.validation;

import com.stockManagement.exception.InvalidDataException;
import com.stockManagement.exception.ResourceNotFoundException;
import com.stockManagement.models.Product;
import com.stockManagement.utilities.StringUtil;

public class ProductValidator {


	public static void validateForCreate(Product p) {
		if (p == null)
			throw new InvalidDataException("Product body is required");

		req("id", p.getId());
		req("name", p.getName());
		req("status", p.getStatus());
		validateStatus(p.getStatus());

	}

	public static void validateForBulk(java.util.List<Product> list) {
		if (list == null || list.isEmpty()) {
			throw new ResourceNotFoundException("Products list is required");
		}
		for (Product p : list)
			validateForCreate(p);
	}

	public static void validateForStatusUpdate(String id, String status) {
		req("id", id);
		req("status", status);
		validateStatus(status);
	}

	private static void req(String field, String value) {
		if (StringUtil.isBlank(value)) {
			throw new InvalidDataException(field + " is required");
		}
	}

	private static void validateStatus(String status) {
		if (!"A".equals(status) && !"I".equals(status)) {
			throw new InvalidDataException("status must be 'A' or 'I'");
		}
	}

	private ProductValidator() {
	}
}
