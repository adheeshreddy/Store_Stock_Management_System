package com.stockManagement.dao.interfaces;

import java.util.List;

import com.stockManagement.models.Product;

public interface ProductDao {
	
	public String saveProduct(Product product);
	public String updateProduct(Product product);
	public List<Product> findAll(String product);
	public Product findById(String id);
	public boolean existsById(String id);
	public boolean existsByName(String name);
	
}
