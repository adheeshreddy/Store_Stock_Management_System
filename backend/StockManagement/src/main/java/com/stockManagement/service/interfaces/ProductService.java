package com.stockManagement.service.interfaces;

import java.util.List;

import com.stockManagement.models.Product;

public interface ProductService {
	
    public String addProduct(Product product);
    public String updateProduct(String status , String  id);
    public List<String> addProducts(List<Product> products);
    public List<Product> getAllProducts(String product);
    public String getProduct(String id);
    
}
