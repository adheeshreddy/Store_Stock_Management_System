package com.stockManagement.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.stockManagement.dao.interfaces.impl.ProductDaoImpl;
import com.stockManagement.exception.InvalidDataException;
import com.stockManagement.exception.ResourceAlreadyExistsException;
import com.stockManagement.exception.ResourceNotFoundException;
import com.stockManagement.models.Product;
import com.stockManagement.service.interfaces.impl.ProductServiceImpl;

public class ProductUnitTests {

	@Mock
	private ProductDaoImpl productDao;

	@InjectMocks
	private ProductServiceImpl productService;

	private Product sampleProduct;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		sampleProduct = Product.builder().id("P101").name("Paracetamol").status("A").build();
	}

	@Test
    void testAddProduct_Success() {
        when(productDao.saveProduct(sampleProduct)).thenReturn(sampleProduct.getId());

        String result = productService.addProduct(sampleProduct);

        assertNotNull(result);
        assertEquals("P101", result);
        verify(productDao, times(1)).saveProduct(sampleProduct);
    }

	@Test
	void testAddProduct_NullProduct_ThrowsException() {
		Exception ex = assertThrows(InvalidDataException.class, () -> productService.addProduct(null));
		assertTrue(ex.getMessage().contains("Product body is required"));
	}

	@Test
    void testAddProductList_Success() {
        when(productDao.saveProduct(sampleProduct)).thenReturn(sampleProduct.getId());

        List<String> result = productService.addProducts(Arrays.asList(sampleProduct));

        assertEquals(1, result.size());
        verify(productDao, times(1)).saveProduct(sampleProduct);
    }

	@Test
    void testUpdateProduct_Success() {
        when(productDao.findById("P101")).thenReturn(sampleProduct);
        Product updatedProduct = Product.builder()
                .id("P101")
                .name("Paracetamol")
                .status("I")
                .build();
        when(productDao.updateProduct(updatedProduct)).thenReturn(updatedProduct.getStatus());

        String updated = productService.updateProduct("I", "P101");

        assertEquals("I", updated);
        verify(productDao, times(1)).findById("P101");
        verify(productDao, times(1)).updateProduct(any(Product.class));
    }

	@Test
    void testUpdateProduct_NotFound_ThrowsException() {
        when(productDao.findById("P999")).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> productService.updateProduct("A", "P999"));

        verify(productDao, times(1)).findById("P999");
        verify(productDao, never()).updateProduct(any(Product.class));
    }

	@Test
    void testGetAllProducts() {
        when(productDao.findAll("%Paracetamol%")).thenReturn(Arrays.asList(sampleProduct));
        
        List<Product> products = productService.getAllProducts("Paracetamol");
        assertEquals(1, products.size());
        verify(productDao, times(1)).findAll("%Paracetamol%");
    }
	
	@Test
	void testAddProduct_DuplicateId_ThrowsException() {
		when(productDao.existsById("P101")).thenReturn(true);

		Exception ex = assertThrows(ResourceAlreadyExistsException.class, () -> productService.addProduct(sampleProduct));
		assertTrue(ex.getMessage().contains("Product already exists"));
		verify(productDao, never()).saveProduct(any());
	}

	@Test
	void testAddProduct_DuplicateName_ThrowsException() {
		when(productDao.existsByName("Paracetamol")).thenReturn(true);

		Exception ex = assertThrows(ResourceAlreadyExistsException.class, () -> productService.addProduct(sampleProduct));
		assertTrue(ex.getMessage().contains("Product already exists"));
		verify(productDao, never()).saveProduct(any());
	}

	@Test
	void testAddProducts_InvalidList_ThrowsException() {
		Exception ex = assertThrows(RuntimeException.class, () -> productService.addProducts(null));
		assertTrue(ex.getMessage().contains("Products list is required"));
		verify(productDao, never()).saveProduct(any());
	}

	@Test
	void testUpdateProduct_InvalidStatus_ThrowsException() {
		Exception ex = assertThrows(InvalidDataException.class, () -> productService.updateProduct(null, "P101"));
		assertTrue(ex.getMessage().contains("status is required"));
		verify(productDao, never()).findById(any());
		verify(productDao, never()).updateProduct(any());
	}

	@Test
	void testGetProduct_Success() {
		when(productDao.findById("P101")).thenReturn(sampleProduct);

		String name = productService.getProduct("P101");
		assertEquals("Paracetamol", name);
		verify(productDao, times(1)).findById("P101");
	}

	@Test
	void testGetProduct_NullResult_ThrowsException() {
		when(productDao.findById("P999")).thenReturn(null);

		assertThrows(RuntimeException.class, () -> productService.getProduct("P999"));
		verify(productDao, times(1)).findById("P999");
	}
}
