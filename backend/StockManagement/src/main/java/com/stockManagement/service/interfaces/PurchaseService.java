package com.stockManagement.service.interfaces;

import java.util.List;

import com.stockManagement.models.PurchaseHeader;
import com.stockManagement.models.PurchaseDetails;

public interface PurchaseService {

    public Long createPurchase(PurchaseHeader header);
    public List<PurchaseHeader> getPurchaseHeaders();
    public List<PurchaseDetails> getDetails(String searchTerm);
    public boolean updateQuantity(Long batchId,int reservedQuantity, String change);
	public List<PurchaseDetails> getBatchesByProductId(String productId);
    
}
