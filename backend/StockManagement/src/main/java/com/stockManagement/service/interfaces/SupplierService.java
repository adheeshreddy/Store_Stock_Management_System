package com.stockManagement.service.interfaces;

import java.util.List;
import java.util.Map;

import com.stockManagement.models.Supplier;

public interface SupplierService {
	
	public Long addSupplier(Supplier supplier);
	public Long updateSupplier(Long id, Supplier supplier);
	public List<Supplier> getSuppliers(String searchKey);
	public String getSupplierById(Long id);
	public Map<Long,String> getAllSuppliers();
}
