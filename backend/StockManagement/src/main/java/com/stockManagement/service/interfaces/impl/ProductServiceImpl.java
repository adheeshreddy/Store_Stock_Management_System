package com.stockManagement.service.interfaces.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stockManagement.dao.interfaces.ProductDao;
import com.stockManagement.exception.ResourceAlreadyExistsException;
import com.stockManagement.exception.ResourceNotFoundException;
import com.stockManagement.models.Product;
import com.stockManagement.service.interfaces.ProductService;
import com.stockManagement.utilities.StringUtil;
import com.stockManagement.validation.ProductValidator;

@Service
public class ProductServiceImpl implements ProductService {

	private final ProductDao productDao;

	public ProductServiceImpl(ProductDao productDao) {
		this.productDao = productDao;
	}

	@Override
	@Transactional
	public String addProduct(Product product) {
		ProductValidator.validateForCreate(product);
		if (productDao.existsById(product.getId()) || productDao.existsByName(product.getName())) {
			throw new ResourceAlreadyExistsException("Product already exists for product Id :"+product.getId());
		}
		return productDao.saveProduct(product);
	}

	@Override
	@Transactional
	public List<String> addProducts(List<Product> products) {
		ProductValidator.validateForBulk(products);
		return products.stream().map(this::addProduct).toList();
	}

	@Override
	@Transactional
	public String updateProduct(String status, String id) {
		ProductValidator.validateForStatusUpdate(id, status);
		Product existing = productDao.findById(id);
		if (existing == null) {
			throw new ResourceNotFoundException("Product not found: " + id);
		}
		existing.setStatus(status);
		return productDao.updateProduct(existing);
	}

	@Override
	@Transactional
	public List<Product> getAllProducts(String searchTerm) {
		String like = (StringUtil.isBlank(searchTerm)) ? "%" : "%" + searchTerm.trim() + "%";
		return productDao.findAll(like);
	}

	@Override
	@Transactional
	public String getProduct(String id) {
		return productDao.findById(id).getName();
	}
}
