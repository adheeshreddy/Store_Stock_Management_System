package com.stockManagement.service.interfaces.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stockManagement.dao.interfaces.PurchaseDao;
import com.stockManagement.exception.InvalidDataException;
import com.stockManagement.models.PurchaseDetails;
import com.stockManagement.models.PurchaseHeader;
import com.stockManagement.service.interfaces.PurchaseService;
import com.stockManagement.utilities.StringUtil;
import com.stockManagement.validation.PurchaseValidator;

@Service
public class PurchaseServiceImpl implements PurchaseService {

    @Autowired
    private PurchaseDao purchaseDao;

    @Override
    @Transactional
    public Long createPurchase(PurchaseHeader header) {
        PurchaseValidator.validateForCreate(header);
        return purchaseDao.createPurchase(header);
    }

    @Override
    @Transactional
    public List<PurchaseDetails> getDetails(String searchTerm) {
        try {
            Long headerId = Long.parseLong(searchTerm);
            return purchaseDao.getDetailsByHeaderId(headerId);
        } catch (NumberFormatException e) {
            return purchaseDao.getDetailsByProductId(searchTerm);
        }
    }

    @Override
    @Transactional
    public List<PurchaseHeader> getPurchaseHeaders() {
        return purchaseDao.getPurchaseHeaders();
    }

    @Override
    @Transactional
    public boolean updateQuantity(Long batchId, int reservedQuantity, String change) {
        if (StringUtil.isBlank(batchId) || reservedQuantity < 0) {
            throw new InvalidDataException("Invalid batch ID or reserved quantity");
        }
        return purchaseDao.updateQuantity(batchId, reservedQuantity, change);
    }

	@Override
    @Transactional
	public List<PurchaseDetails> getBatchesByProductId(String productId) {
		 return purchaseDao.getDetailsByProductId(productId);
	}
}
