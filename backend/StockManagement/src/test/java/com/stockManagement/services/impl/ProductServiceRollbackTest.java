package com.stockManagement.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.stockManagement.exception.InvalidDataException;
import com.stockManagement.exception.ResourceAlreadyExistsException;
import com.stockManagement.models.Product;
import com.stockManagement.service.interfaces.ProductService;

@SpringBootTest
@Transactional
public class ProductServiceRollbackTest {

	@Autowired
	private ProductService productService;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void testRollbackOnDuplicateProduct() {
		Product product = Product.builder().id("P501").name("Ibuprofenol").status("A").build();

		String id = productService.addProduct(product);
		assertEquals("P501", id);

		Product duplicate = Product.builder().id("P501").name("Ibuprofi").status("A").build();
		assertThrows(ResourceAlreadyExistsException.class, () -> productService.addProduct(duplicate));

		List<Product> products = productService.getAllProducts("Ibuprofenol");
		assertEquals(1, products.size());
		assertEquals("Ibuprofenol", products.get(0).getName());
	}

	@Test
	void testRollbackOnInvalidProducts() {
		Product p1 = Product.builder().id("P999").name("Ibuprofen").status("A").build();
		Product p2 = Product.builder().id("P999").name("Dermicool").status("A").build();
		List<Product> products = List.of(p1, p2);
		int beforeCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tbl_product", Integer.class);
		//System.out.println(beforeCount + " before insertion");
		assertThrows(ResourceAlreadyExistsException.class, () -> productService.addProducts(products));
		int afterCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tbl_product", Integer.class);
		//System.out.println(afterCount + " After insertion");
		assertEquals(beforeCount, afterCount);

	}
	
	@Test
	void testRollbackOnUpdateProduct() {
		Product product = Product.builder().id("P501").name("Ibuprofenol").status("A").build();
		productService.addProduct(product);
		product.setStatus(null);
		int beforeUpdate=jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tbl_product", Integer.class);
		int beforeUpdateLog=jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tbl_productLog", Integer.class);
//		System.out.println(beforeUpdate+" product table before update");
//		System.out.println(beforeUpdateLog+" product log table before update");
		assertThrows(InvalidDataException.class,()-> productService.updateProduct(product.getStatus(), product.getId()));
		int afterUpdate=jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tbl_product", Integer.class);
		int afterUpdateLog=jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tbl_productLog", Integer.class);
//		System.out.println(afterUpdateLog+" product log table after update");
//		System.out.println(afterUpdate+" product table after update");
		assertEquals(beforeUpdate, afterUpdate);
		assertEquals(beforeUpdateLog, afterUpdateLog);
	}
	
	
}
