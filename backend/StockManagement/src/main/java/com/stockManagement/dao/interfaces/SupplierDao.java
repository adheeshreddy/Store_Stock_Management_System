package com.stockManagement.dao.interfaces;

import java.util.List;

import com.stockManagement.models.Supplier;

public interface SupplierDao {
	public Long saveSupplier(Supplier s);
	public Long update(Supplier s);
	public Supplier findById(Long id);   
	public  List<Supplier> findByKey(String key);   
	public List<Supplier> findAll();
	public boolean existsByMobile(String mobile);
	public boolean existsByEmail(String email);
}
