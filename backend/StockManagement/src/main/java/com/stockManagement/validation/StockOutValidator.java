package com.stockManagement.validation;

import java.util.List;

import com.stockManagement.exception.InvalidDataException;
import com.stockManagement.models.StockOutDetails;
import com.stockManagement.models.StockOutHeader;

public class StockOutValidator {

	private StockOutValidator() {
	}

	public static void validateForCreate(StockOutHeader h) {
		if (h == null)
			throw new InvalidDataException("StockOut header is required");
		if (h.getHeaderId() == null)
			throw new InvalidDataException("headerId is required");
		if (h.getSupplierId() == null)
			throw new InvalidDataException("supplierId is required");
		if (h.getApprovedAt() == null)
			throw new InvalidDataException("approvedAt is required");
		if (h.getApprovedBy() == null || h.getApprovedBy().isBlank())
			throw new InvalidDataException("approvedBy is required");

		if (h.getStockOutDetails() == null || h.getStockOutDetails().isEmpty())
			throw new InvalidDataException("stockOutDetails cannot be empty");

		for (StockOutDetails d : h.getStockOutDetails()) {
			if (d.getProductId() == null || d.getProductId().isBlank())
				throw new InvalidDataException("productId is required");
			if (d.getQuantity() == null || d.getQuantity() <= 0)
				throw new InvalidDataException("quantity must be > 0");
		}
	}

	public static void recomputeTotalsIfZero(StockOutHeader h) {
		double tt = 0, tg = 0, ta = 0;
		List<StockOutDetails> lines = h.getStockOutDetails();
		if (lines != null) {
			for (StockOutDetails d : lines) {
				tt += n(d.getTaxableAmount());
				tg += n(d.getGstAmount());
				ta += n(d.getTotalAmount());
			}
		}
		if (isZeroOrNull(h.getTotalTaxableAmount()))
			h.setTotalTaxableAmount(tt);
		if (isZeroOrNull(h.getTotalGst()))
			h.setTotalGst(tg);
		if (isZeroOrNull(h.getTotalAmount()))
			h.setTotalAmount(ta);
	}

	private static double n(Double v) {
		return v == null ? 0d : v;
	}

	private static boolean isZeroOrNull(Double v) {
		return v == null || Math.abs(v) < 1e-9;
	}
}
