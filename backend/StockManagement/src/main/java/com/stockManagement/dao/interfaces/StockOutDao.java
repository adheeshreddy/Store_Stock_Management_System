package com.stockManagement.dao.interfaces;

import java.util.List;

import com.stockManagement.models.StockOutDetails;
import com.stockManagement.models.StockOutHeader;

public interface StockOutDao {
	public StockOutHeader createStockOut(StockOutHeader header);
	public  List<StockOutHeader> getStockOutHeaders();
	public List<StockOutHeader> getHeadersBySupplierId(Long headerId);
	public List<StockOutDetails> getDetailsByStockId(Long headerId);
	public List<StockOutHeader> getHeadersByProformaId(Long headerId);
}
