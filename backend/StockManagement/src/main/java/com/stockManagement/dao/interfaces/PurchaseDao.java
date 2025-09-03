package com.stockManagement.dao.interfaces;

import java.util.List;

import com.stockManagement.models.PurchaseHeader;
import com.stockManagement.models.PurchaseDetails;

public interface PurchaseDao {

	public Long createPurchase(PurchaseHeader header);
	public List<PurchaseHeader> getPurchaseHeaders();
	public List<PurchaseDetails> getDetailsByHeaderId(Long headerId);
	public List<PurchaseDetails> getDetailsByProductId(String productId);
	public boolean updateQuantity(Long batchId, int reservedQuantity,String change);

}
