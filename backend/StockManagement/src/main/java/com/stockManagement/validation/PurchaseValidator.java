package com.stockManagement.validation;

import java.util.List;

import com.stockManagement.exception.InvalidDataException;
import com.stockManagement.models.PurchaseDetails;
import com.stockManagement.models.PurchaseHeader;

public final class PurchaseValidator {
	private PurchaseValidator() {
	}

	public static void validateForCreate(PurchaseHeader h) {
		if (h == null)
			throw new InvalidDataException("Purchase body is required");
		req("supplierId", h.getSupplierId());
		req("createdBy", h.getCreatedBy());
		if (h.getPurchaseDetails() == null || h.getPurchaseDetails().isEmpty()) {
			throw new InvalidDataException("At least one purchase detail is required");
		}
		validateDetails(h.getPurchaseDetails());
	}

	public static void validateDetails(List<PurchaseDetails> list) {
		for (PurchaseDetails d : list) {
			req("productId", d.getProductId());
			pos("quantity", d.getQuantity());
			posOrZero("taxableAmount", d.getTaxableAmount());
			posOrZero("gstAmount", d.getGstAmount());
			posOrZero("totalAmount", d.getTotalAmount());
			if (d.getExpiry() == null) {
				throw new InvalidDataException("expiry is required");
			}
		}
	}

	private static void req(String field, Object val) {
		if (val == null || (val instanceof String s && s.trim().isEmpty())) {
			throw new InvalidDataException(field + " is required");
		}
	}

	private static void pos(String field, Integer val) {
		if (val == null || val <= 0)
			throw new InvalidDataException(field + " must be > 0");
	}

	private static void posOrZero(String field, Double val) {
		if (val == null || val < 0)
			throw new InvalidDataException(field + " must be >= 0");
	}
}