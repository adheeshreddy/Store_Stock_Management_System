package com.stockManagement.validation;

import static com.stockManagement.validation.ValidationUtil.requireNotBlank;
import static com.stockManagement.validation.ValidationUtil.requireNotNull;
import static com.stockManagement.validation.ValidationUtil.requirePositive;

import java.util.List;

import org.springframework.stereotype.Component;

import com.stockManagement.exception.InvalidDataException;
import com.stockManagement.models.ProformaDetails;
import com.stockManagement.models.ProformaHeader;

@Component
public class ProformaValidator {
	
	public static void validateCreateOrEdit(ProformaHeader h) {
        requireNotNull(h, "Proforma body");
        requireNotNull(h.getSupplierId(), "supplierId");
        requireNotBlank(h.getCreatedBy(), "createdBy");
        requireNotNull(h.getProformaDetails(), "details");

        List<ProformaDetails> list = h.getProformaDetails();
        if (list.isEmpty()) throw new InvalidDataException("details cannot be empty");

        for (ProformaDetails d : list) {
            requireNotBlank(d.getProductId(), "details.productId");
            requireNotNull(d.getExpiry(), "details.expiry");
            requirePositive(d.getQuantity(), "details.quantity");
            requirePositive(d.getTaxableAmount(), "details.taxableAmount");
            requirePositive(d.getGstAmount(), "details.gstAmount");
            requirePositive(d.getTotalAmount(), "details.totalAmount");
        }
    }

    public static void validateDelete(ProformaHeader h) {
        requireNotNull(h, "Proforma body");
        requireNotNull(h.getId(), "id");
        if (h.getCreatedBy() != null) requireNotBlank(h.getCreatedBy(), "createdBy");
    }

    public static void validateApprove(ProformaHeader h) {
        requireNotNull(h, "Proforma body");
        requireNotNull(h.getId(), "id");
        if (h.getCreatedBy() != null) requireNotBlank(h.getCreatedBy(), "createdBy");
    }
}
