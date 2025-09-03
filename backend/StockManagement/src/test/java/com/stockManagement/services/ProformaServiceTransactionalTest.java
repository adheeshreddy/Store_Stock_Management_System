package com.stockManagement.services;

import com.stockManagement.dao.interfaces.ProformaDao;
import com.stockManagement.exception.BadRequestException;
import com.stockManagement.models.ProformaDetails;
import com.stockManagement.models.ProformaHeader;
import com.stockManagement.service.interfaces.ProformaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProformaServiceTransactionalTest {

    @Autowired
    private ProformaService proformaService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final Long TEST_BATCH_ID = 1L;
    private final int INITIAL_STOCK_QUANTITY = 100;

    @BeforeEach
    void setUp() {
        // @Transactional handles the cleanup, so we only need to set up the base state.
        // We will manually insert a stock item to work with.
        jdbcTemplate.update("DELETE FROM tbl_PurchaseStock");
        jdbcTemplate.update("INSERT INTO tbl_PurchaseStock (id, itemName, batchId, quantity, mfgDate, expDate, taxableAmount, gstAmount, totalAmount) VALUES (?, 'TestItem', ?, ?, '2025-01-01', '2026-01-01', 1000.0, 180.0, 1180.0)",
                TEST_BATCH_ID, TEST_BATCH_ID, INITIAL_STOCK_QUANTITY);
    }

    private ProformaHeader createProformaHeader(int quantity) {
        ProformaDetails details = new ProformaDetails();
        details.setBatchId(TEST_BATCH_ID);
        details.setQuantity(quantity);

        ProformaHeader header = new ProformaHeader();
        header.setSupplierId(1L);
        header.setProformaDetails(Collections.singletonList(details));
        header.setCreatedBy("testUser");
        return header;
    }

    private int getStockQuantity() {
        return jdbcTemplate.queryForObject("SELECT quantity FROM tbl_PurchaseStock WHERE batchId = ?", Integer.class, TEST_BATCH_ID);
    }

    private int getProformaCount() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tbl_ProformaHeader", Integer.class);
    }

    // --- CREATE TESTS ---

    @Test
    void createProforma_Success_TransactionCommitted() {
        int quantityToAdd = 10;
        ProformaHeader proformaHeader = createProformaHeader(quantityToAdd);

        Long id = proformaService.createProforma(proformaHeader);
        assertNotNull(id);

        // Verify a new record exists in the ProformaHeader table
        assertEquals(1, getProformaCount());
        
        // Verify the quantity of the purchase stock was increased
        assertEquals(INITIAL_STOCK_QUANTITY + quantityToAdd, getStockQuantity());
    }

    @Test
    void createProforma_InvalidData_TransactionRolledBack() {
        // Simulate a validation failure by using invalid quantity
        ProformaHeader proformaHeader = createProformaHeader(-10);

        // We need to catch the exception to verify the rollback
        assertThrows(BadRequestException.class, () -> proformaService.createProforma(proformaHeader));

        // Verify no record was inserted into ProformaHeader
        assertEquals(0, getProformaCount());
        
        // Verify no change was made to the purchase stock quantity
        assertEquals(INITIAL_STOCK_QUANTITY, getStockQuantity());
    }
    
    // --- UPDATE TESTS ---

    @Test
    void editProforma_Success_TransactionCommitted() {
        // First, create a proforma to edit
        ProformaHeader initialProforma = createProformaHeader(10);
        Long proformaId = proformaService.createProforma(initialProforma);
        
        // Prepare updates
        int newQuantity = 5;
        ProformaHeader updateHeader = new ProformaHeader();
        updateHeader.setId(proformaId);
        ProformaDetails updatedDetails = new ProformaDetails();
        updatedDetails.setBatchId(TEST_BATCH_ID);
        updatedDetails.setQuantity(newQuantity); // New quantity is less
        updateHeader.setProformaDetails(Collections.singletonList(updatedDetails));
        
        proformaService.editProforma(updateHeader);
        
        List<Map<String, Object>> details = jdbcTemplate.queryForList("SELECT quantity FROM tbl_ProformaDetails WHERE proformaId = ?", proformaId);
        assertEquals(newQuantity, details.get(0).get("quantity"));
        
        int expectedQuantity = INITIAL_STOCK_QUANTITY + 10 - newQuantity;
        assertEquals(expectedQuantity, getStockQuantity());
    }
    
    @Test
    void editProforma_InvalidData_TransactionRolledBack() {
        ProformaHeader initialProforma = createProformaHeader(10);
        Long proformaId = proformaService.createProforma(initialProforma);
        
        ProformaHeader updateHeader = new ProformaHeader();
        updateHeader.setId(proformaId);
        ProformaDetails updatedDetails = new ProformaDetails();
        updatedDetails.setBatchId(TEST_BATCH_ID);
        updatedDetails.setQuantity(-5); 
        updateHeader.setProformaDetails(Collections.singletonList(updatedDetails));
        
        assertThrows(BadRequestException.class, () -> proformaService.editProforma(updateHeader));
        
        List<Map<String, Object>> details = jdbcTemplate.queryForList("SELECT quantity FROM tbl_ProformaDetails WHERE proformaId = ?", proformaId);
        assertEquals(10, details.get(0).get("quantity")); 
        
        int expectedQuantity = INITIAL_STOCK_QUANTITY + 10;
        assertEquals(expectedQuantity, getStockQuantity()); 
    }
}