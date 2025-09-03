package com.stockManagement.services;
import com.stockManagement.dao.interfaces.ProformaDao;
import com.stockManagement.dao.interfaces.PurchaseDao;
import com.stockManagement.dao.interfaces.PurchaseLookupDao;
import com.stockManagement.exception.BadRequestException;
import com.stockManagement.exception.ResourceNotFoundException;
import com.stockManagement.models.ProformaDetails;
import com.stockManagement.models.ProformaHeader;
import com.stockManagement.models.StockOutHeader;
import com.stockManagement.service.interfaces.StockOutService;
import com.stockManagement.service.interfaces.impl.ProformaServiceImpl;
import com.stockManagement.validation.ProformaValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProformaServiceIntegrationTest {

    @Mock
    private ProformaDao proformaDao;
    @Mock
    private PurchaseDao purchaseStockDao;
    @Mock
    private PurchaseLookupDao purchaseLookupDao;
    @Mock
    private StockOutService stockOutService;
    @Mock
    private ProformaValidator validator;

    @InjectMocks
    private ProformaServiceImpl proformaService;

    private ProformaHeader proformaHeader;
    private ProformaDetails proformaDetails;

    @BeforeEach
    void setUp() {
        proformaDetails = new ProformaDetails();
        proformaDetails.setBatchId(1L);
        proformaDetails.setQuantity(10);
        proformaDetails.setTaxableAmount(100.0);
        proformaDetails.setGstAmount(18.0);
        proformaDetails.setTotalAmount(118.0);

        proformaHeader = new ProformaHeader();
        proformaHeader.setSupplierId(101L);
        proformaHeader.setProformaDetails(Collections.singletonList(proformaDetails));
        proformaHeader.setCreatedBy("testUser");
        proformaHeader.setId(1L);
    }

    // --- CREATE TESTS ---

    @Test
    @Transactional
    void createProforma_Success() {
        when(proformaDao.insertProforma(any(ProformaHeader.class))).thenReturn(1L);

        Long id = proformaService.createProforma(proformaHeader);

        assertEquals(1L, id);
        verify(validator).validateCreateOrEdit(proformaHeader);
        verify(proformaDao).insertProforma(proformaHeader);
        verify(purchaseStockDao).updateQuantity(proformaDetails.getBatchId(), proformaDetails.getQuantity(), "increase");
    }

    @Test
    @Transactional
    void createProforma_ThrowsException_Rollback() {
        doThrow(new BadRequestException("Validation error")).when(validator).validateCreateOrEdit(any(ProformaHeader.class));

        assertThrows(BadRequestException.class, () -> proformaService.createProforma(proformaHeader));
        verify(validator).validateCreateOrEdit(proformaHeader);
        verify(proformaDao, never()).insertProforma(any(ProformaHeader.class));
        verify(purchaseStockDao, never()).updateQuantity(anyLong(), anyInt(), anyString());
    }

    // --- EDIT TESTS ---

    @Test
    @Transactional
    void editProforma_Success() {
        ProformaHeader existing = new ProformaHeader();
        existing.setId(1L);
        ProformaDetails oldDetails = new ProformaDetails();
        oldDetails.setBatchId(2L);
        oldDetails.setQuantity(5);
        existing.setProformaDetails(Collections.singletonList(oldDetails));
        
        when(proformaDao.findById(1L, true)).thenReturn(existing);
        when(proformaDao.updateProforma(any(ProformaHeader.class))).thenReturn(1L);

        Long id = proformaService.editProforma(proformaHeader);

        assertEquals(1L, id);
        verify(proformaDao).findById(1L, true);
        verify(purchaseStockDao).updateQuantity(oldDetails.getBatchId(), oldDetails.getQuantity(), "decrease");
        verify(purchaseStockDao).updateQuantity(proformaDetails.getBatchId(), proformaDetails.getQuantity(), "increase");
        verify(proformaDao).updateProforma(proformaHeader);
    }

    @Test
    @Transactional
    void editProforma_NotFound_ThrowsException_Rollback() {
        when(proformaDao.findById(1L, true)).thenReturn(null);

        assertThrows(BadRequestException.class, () -> proformaService.editProforma(proformaHeader));
        verify(proformaDao).findById(1L, true);
        verify(purchaseStockDao, never()).updateQuantity(anyLong(), anyInt(), anyString());
        verify(proformaDao, never()).updateProforma(any(ProformaHeader.class));
    }

    // --- DELETE TESTS ---

    @Test
    @Transactional
    void deleteProforma_Success() {
        ProformaHeader existing = new ProformaHeader();
        existing.setId(1L);
        ProformaDetails details = new ProformaDetails();
        details.setBatchId(1L);
        details.setQuantity(10);
        existing.setProformaDetails(Collections.singletonList(details));
        
        when(proformaDao.findById(1L, true)).thenReturn(existing);
        when(proformaDao.softDelete(1L, "testUser")).thenReturn(1L);

        Long id = proformaService.deleteProforma(proformaHeader);

        assertEquals(1L, id);
        verify(validator).validateDelete(proformaHeader);
        verify(proformaDao).findById(1L, true);
        verify(purchaseStockDao).updateQuantity(details.getBatchId(), details.getQuantity(), "decrease");
        verify(proformaDao).softDelete(1L, "testUser");
    }

    @Test
    @Transactional
    void deleteProforma_NotFound_ThrowsException_Rollback() {
        when(proformaDao.findById(1L, true)).thenReturn(null);

        assertThrows(BadRequestException.class, () -> proformaService.deleteProforma(proformaHeader));
        verify(proformaDao).findById(1L, true);
        verify(purchaseStockDao, never()).updateQuantity(anyLong(), anyInt(), anyString());
        verify(proformaDao, never()).softDelete(anyLong(), anyString());
    }

    // --- APPROVE TESTS ---

    @Test
    @Transactional
    void approveProforma_Success_InvoiceAndReturn() {
        ProformaHeader existing = new ProformaHeader();
        existing.setId(1L);
        existing.setSupplierId(101L);
        existing.setStatus("C");
        
        ProformaDetails invoiceDetail = new ProformaDetails();
        invoiceDetail.setBatchId(1L);
        invoiceDetail.setQuantity(10);
        invoiceDetail.setTaxableAmount(100.0);
        invoiceDetail.setGstAmount(18.0);
        invoiceDetail.setTotalAmount(118.0);
        
        ProformaDetails returnDetail = new ProformaDetails();
        returnDetail.setBatchId(2L);
        returnDetail.setQuantity(5);
        returnDetail.setTaxableAmount(50.0);
        returnDetail.setGstAmount(9.0);
        returnDetail.setTotalAmount(59.0);
        
        existing.setProformaDetails(List.of(invoiceDetail, returnDetail));

        when(proformaDao.findById(1L, true)).thenReturn(existing);
        when(proformaDao.approveProforma(1L)).thenReturn(existing);
        when(purchaseLookupDao.existsBySupplierAndBatch(101L, 1L)).thenReturn(false); 
        when(purchaseLookupDao.existsBySupplierAndBatch(101L, 2L)).thenReturn(true); 

        ProformaHeader approvedHeader = proformaService.approveProforma(proformaHeader);

        assertNotNull(approvedHeader);
        verify(validator).validateApprove(proformaHeader);
        verify(proformaDao).findById(1L, true);
        verify(proformaDao).approveProforma(1L);
        verify(stockOutService, times(2)).createStockOut(any(StockOutHeader.class));
        verify(purchaseStockDao).updateQuantity(invoiceDetail.getBatchId(), invoiceDetail.getQuantity(), "approved");
        verify(purchaseStockDao).updateQuantity(returnDetail.getBatchId(), returnDetail.getQuantity(), "approved");
    }

    @Test
    @Transactional
    void approveProforma_DeletedStatus_ThrowsException_Rollback() {
        ProformaHeader deletedHeader = new ProformaHeader();
        deletedHeader.setId(1L);
        deletedHeader.setStatus("D");

        when(proformaDao.findById(1L, true)).thenReturn(deletedHeader);

        assertThrows(IllegalStateException.class, () -> proformaService.approveProforma(proformaHeader));
        verify(proformaDao).findById(1L, true);
        verify(proformaDao, never()).approveProforma(anyLong());
        verify(stockOutService, never()).createStockOut(any(StockOutHeader.class));
    }
}


/*
 * package com.stockManagement.services;

import com.stockManagement.models.ProformaDetails;
import com.stockManagement.models.ProformaHeader;
import com.stockManagement.service.interfaces.ProformaService;
import com.stockManagement.dao.interfaces.ProformaDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProformaServiceIntegrationTest {

    @Autowired
    private ProformaService proformaService;

    @Autowired
    private ProformaDao proformaDao;

    private ProformaHeader createSampleHeader() {
        ProformaDetails d1 = new ProformaDetails();
        d1.setBatchId(1L);   // must exist in purchases
        d1.setProductId("P101");
        d1.setQuantity(5);
        d1.setTaxableAmount(100.0);
        d1.setGstAmount(18.0);
        d1.setTotalAmount(118.0);
        d1.setExpiry(LocalDate.parse("2026-12-31"));

        ProformaHeader h = new ProformaHeader();
        h.setSupplierId(1L);  // must exist in tbl_supplier
        h.setCreatedBy("admin");
        h.setCreatedAt(LocalDate.now());
        h.setProformaDetails(List.of(d1));
        return h;
    }

    @Test
    @Rollback
    void createProforma_insertsHeaderAndDetails() {
        ProformaHeader header = createSampleHeader();

        Long id = proformaService.createProforma(header);
        assertNotNull(id);

        ProformaHeader stored = proformaDao.findById(id, true);
        assertNotNull(stored);
        assertEquals("C", stored.getStatus());
        assertEquals(1, stored.getProformaDetails().size());
    }

    @Test
    @Rollback
    void editProforma_logsPreviousAndUpdates() {
        // first create
        ProformaHeader header = createSampleHeader();
        Long id = proformaService.createProforma(header);

        // edit with new qty
        ProformaHeader update = proformaDao.findById(id, true);
        update.getProformaDetails().get(0).setQuantity(10);

        Long updatedId = proformaService.editProforma(update);
        assertEquals(id, updatedId);

        // check log was written
        // you can query tbl_proformaLogHeader and tbl_proformaLogDetails here
        ProformaHeader after = proformaDao.findById(id, true);
        assertEquals(10, after.getProformaDetails().get(0).getQuantity());
    }

    @Test
    @Rollback
    void deleteProforma_marksDeletedAndLogs() {
        ProformaHeader header = createSampleHeader();
        Long id = proformaService.createProforma(header);

        ProformaHeader del = proformaDao.findById(id, true);
        del.setCreatedBy("admin");
        Long deletedId = proformaService.deleteProforma(del);

        assertEquals(id, deletedId);
        ProformaHeader after = proformaDao.findById(id, true);
        assertEquals("D", after.getStatus()); // soft delete
    }

    @Test
    @Rollback
    void createProforma_rollbackOnFailure() {
        ProformaHeader header = createSampleHeader();
        header.setSupplierId(99999L); // non-existing supplier → FK fail

        assertThrows(Exception.class, () -> proformaService.createProforma(header));

        // rollback → nothing should exist
        assertTrue(proformaDao.findAll(true)
                .stream()
                .noneMatch(h -> h.getSupplierId() == 99999L));
    }
}

 */
