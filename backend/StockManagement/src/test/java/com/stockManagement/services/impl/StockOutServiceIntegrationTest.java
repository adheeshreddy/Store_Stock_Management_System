package com.stockManagement.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.List;

import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.stockManagement.models.StockOutDetails;
import com.stockManagement.models.StockOutHeader;
import com.stockManagement.service.interfaces.StockOutService;

@SpringBootTest
@ActiveProfiles("test") 
@Transactional
public class StockOutServiceIntegrationTest {

    @Autowired
    private StockOutService stockOutService;

    @Test
    void testCreateStockOut_Success() {
        StockOutDetails detail = new StockOutDetails();
        detail.setProductId("P101");
        detail.setQuantity(10);

        StockOutHeader header = new StockOutHeader();
        header.setSupplierId(202L);
        header.setHeaderId(303L);
        header.setStockOutDetails(Collections.singletonList(detail));

        StockOutHeader savedHeader = stockOutService.createStockOut(header);

        assertNotNull(savedHeader);
        assertEquals(202L, savedHeader.getSupplierId());
        assertFalse(savedHeader.getStockOutDetails().isEmpty());
    }

    @Test
    void testCreateStockOut_NullHeader_ThrowsException() {
        assertThrows(BadRequestException.class, () -> stockOutService.createStockOut(null));
    }

    @Test
    void testCreateStockOut_EmptyDetails_ThrowsException() {
        StockOutHeader header = new StockOutHeader();
        header.setSupplierId(202L);
        header.setStockOutDetails(Collections.emptyList());

        assertThrows(BadRequestException.class, () -> stockOutService.createStockOut(header));
    }

    @Test
    void testGetDetailsByStockId_ValidId() {
        List<StockOutDetails> details = stockOutService.getDetailsByStockId(1L);
        assertNotNull(details);
    }

    @Test
    void testGetDetailsByStockId_InvalidId_ThrowsException() {
        assertThrows(BadRequestException.class, () -> stockOutService.getDetailsByStockId(-1L));
    }

    @Test
    void testGetStockOutHeaders() {
        List<StockOutHeader> headers = stockOutService.getStockOutHeaders();
        assertNotNull(headers);
    }

    @Test
    void testGetHeadersBySupplierId() {
        List<StockOutHeader> headers = stockOutService.getHeadersBySupplierId(202L);
        assertNotNull(headers);
    }

    @Test
    void testGetHeadersByProformaId() {
        List<StockOutHeader> headers = stockOutService.getHeadersByProformaId(303L);
        assertNotNull(headers);
    }
    
    
}