package com.stockManagement.services.impl;

import com.stockManagement.dao.interfaces.SupplierDao;
import com.stockManagement.exception.InvalidDataException;
import com.stockManagement.exception.ResourceAlreadyExistsException;
import com.stockManagement.exception.ResourceNotFoundException;
import com.stockManagement.models.Supplier;
import com.stockManagement.service.interfaces.impl.SupplierServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SupplierServiceUnitTest {

    @Mock
    private SupplierDao supplierDao;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    private Supplier sample;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sample = new Supplier(1L, "John", "M", "9876543210", "john@example.com",
                "India", "KA", "Bangalore", "Address", "admin", LocalDateTime.now());
    }

    @Test
    void testAddSupplier_Success() {
        when(supplierDao.existsByMobile("9876543210")).thenReturn(false);
        when(supplierDao.existsByEmail("john@example.com")).thenReturn(false);
        when(supplierDao.saveSupplier(sample)).thenReturn(1L);

        Long id = supplierService.addSupplier(sample);

        assertEquals(1L, id);
        verify(supplierDao, times(1)).saveSupplier(sample);
    }

    @Test
    void testAddSupplier_DuplicateMobile_ThrowsException() {
        when(supplierDao.existsByMobile("9876543210")).thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> supplierService.addSupplier(sample));
        verify(supplierDao, never()).saveSupplier(any());
    }

    @Test
    void testAddSupplier_DuplicateEmail_ThrowsException() {
        when(supplierDao.existsByMobile("9876543210")).thenReturn(false);
        when(supplierDao.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class,
                () -> supplierService.addSupplier(sample));
    }

    @Test
    void testAddSupplier_EmptyFields_ThrowsException() {
        Supplier emptyNameSupplier = new Supplier(1L, "", "M", "9876543210", "john@example.com",
                "India", "KA", "Bangalore", "Address", "admin", LocalDateTime.now());

        assertThrows(InvalidDataException.class, () -> supplierService.addSupplier(emptyNameSupplier));

        verify(supplierDao, never()).saveSupplier(any());
    }
    @Test
    void testUpdateSupplier_Success() {
        Supplier existing = new Supplier(1L, "John", "M", "9876543210", "john@example.com",
                "India", "KA", "Bangalore", "OldAddr", "admin", LocalDateTime.now());

        when(supplierDao.findById(1L)).thenReturn(existing);
        when(supplierDao.update(any(Supplier.class))).thenReturn(1L);

        sample.setAddress("New Address");

        Long updatedId = supplierService.updateSupplier(1L, sample);

        assertEquals(1L, updatedId);
        assertEquals("New Address", existing.getAddress());
        verify(supplierDao, times(1)).update(existing);
    }

    @Test
    void testUpdateSupplier_NotFound_ThrowsException() {
        when(supplierDao.findById(1L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> supplierService.updateSupplier(1L, sample));
    }

    @Test
    void testUpdateSupplier_DuplicateEmail_ThrowsException() {
        Supplier existing = new Supplier(1L, "John", "M", "9876543210", "old@example.com",
                "India", "KA", "Bangalore", "Addr", "admin", LocalDateTime.now());

        when(supplierDao.findById(1L)).thenReturn(existing);
        when(supplierDao.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class,
                () -> supplierService.updateSupplier(1L, sample));
    }


    @Test
    void testGetSuppliers_WithKey() {
        when(supplierDao.findByKey("John")).thenReturn(Arrays.asList(sample));

        List<Supplier> result = supplierService.getSuppliers("John");

        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getName());
    }

    @Test
    void testGetSuppliers_NoKey_ReturnsAll() {
        when(supplierDao.findAll()).thenReturn(Arrays.asList(sample));

        List<Supplier> result = supplierService.getSuppliers(null);

        assertEquals(1, result.size());
    }

    @Test
    void testGetSupplierById_Success() {
        when(supplierDao.findById(1L)).thenReturn(sample);

        String name = supplierService.getSupplierById(1L);

        assertEquals("John", name);
    }

 
    @Test
    void testGetAllSuppliers_Map() {
        when(supplierDao.findAll()).thenReturn(Collections.singletonList(sample));

        Map<Long, String> result = supplierService.getAllSuppliers();

        assertTrue(result.containsKey(1L));
        assertEquals("John", result.get(1L));
    }

   
}