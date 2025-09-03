package com.stockManagement.services;

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
import com.stockManagement.exception.ResourceNotFoundException;
import com.stockManagement.models.Product;
import com.stockManagement.service.interfaces.impl.ProductServiceImpl;

public class ProductTests {

    @Mock
    private ProductDaoImpl productDao;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleProduct = Product.builder()
                .id("P101")
                .name("Paracetamol")
                .status("A")
                .build();
    }

    @Test
    void testAddProduct_Success() {
        when(productDao.save(sampleProduct)).thenReturn(sampleProduct.getId());

        String result = productService.addProduct(sampleProduct);

        assertNotNull(result);
        assertEquals("P101", result);
        verify(productDao, times(1)).save(sampleProduct);
    }

    @Test
    void testAddProduct_NullProduct_ThrowsException() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> productService.addProduct(null));
        assertTrue(ex.getMessage().contains("Invalid Product"));
    }

    @Test
    void testAddProductList_Success() {
        when(productDao.save(sampleProduct)).thenReturn(sampleProduct.getId());

        List<String> result = productService.addProducts(Arrays.asList(sampleProduct));

        assertEquals(1, result.size());
        verify(productDao, times(1)).save(sampleProduct);
    }

    @Test
    void testUpdateProduct_Success() {
        when(productDao.findById("P101")).thenReturn(sampleProduct);
        Product updatedProduct = Product.builder()
                .id("P101")
                .name("Paracetamol")
                .status("I")
                .build();
        when(productDao.update(updatedProduct)).thenReturn(updatedProduct.getStatus());

        String updated = productService.updateProduct("I", "P101");

        assertEquals("I", updated);
        verify(productDao, times(1)).findById("P101");
        verify(productDao, times(1)).update(any(Product.class));
    }

    @Test
    void testUpdateProduct_NotFound_ThrowsException() {
        when(productDao.findById("P999")).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> productService.updateProduct("A", "P999"));

        verify(productDao, times(1)).findById("P999");
        verify(productDao, never()).update(any(Product.class));
    }

    @Test
    void testGetAllProducts() {
        when(productDao.findAll("Paracetamol%")).thenReturn(Arrays.asList(sampleProduct));

        List<Product> products = productService.getAllProducts("Paracetamol");

        assertEquals(1, products.size());
        verify(productDao, times(1)).findAll("Paracetamol%");
    }
}
