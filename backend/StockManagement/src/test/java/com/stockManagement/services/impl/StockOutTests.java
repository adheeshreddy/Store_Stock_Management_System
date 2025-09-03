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
import java.util.Collections;
import java.util.List;

import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.stockManagement.dao.interfaces.StockOutDao;
import com.stockManagement.models.StockOutDetails;
import com.stockManagement.models.StockOutHeader;
import com.stockManagement.service.interfaces.impl.StockOutServiceImpl;

public class StockOutTests {

    @Mock
    private StockOutDao stockOutDao;

    @InjectMocks
    private StockOutServiceImpl stockOutService;

    private StockOutHeader header;
    private StockOutDetails detail;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        detail = new StockOutDetails();
        detail.setHeaderId(101L);
        detail.setProductId("P101");

        header = new StockOutHeader();
        header.setHeaderId(1L);
        header.setStockOutDetails(Arrays.asList(detail));
    }

    @Test
    void testCreateStockOut_Success() {
        when(stockOutDao.createStockOut(header)).thenReturn(header);

        StockOutHeader result = stockOutService.createStockOut(header);

        assertNotNull(result);
        assertEquals(header, result);
        verify(stockOutDao, times(1)).createStockOut(header);
    }

    @Test
    void testCreateStockOut_NullHeader_ThrowsException() {
        assertThrows(BadRequestException.class,
                () -> stockOutService.createStockOut(null));
        verify(stockOutDao, never()).createStockOut(any());
    }

    @Test
    void testCreateStockOut_EmptyDetails_ThrowsException() {
        header.setStockOutDetails(Collections.emptyList());

        assertThrows(BadRequestException.class,
                () -> stockOutService.createStockOut(header));
        verify(stockOutDao, never()).createStockOut(any());
    }

    @Test
    void testCreateStockOut_NullDetails_ThrowsException() {
        header.setStockOutDetails(null);

        assertThrows(BadRequestException.class,
                () -> stockOutService.createStockOut(header));
        verify(stockOutDao, never()).createStockOut(any());
    }

    @Test
    void testGetDetailsByHeaderId_Success() {
        when(stockOutDao.getDetailsByStockId(1L)).thenReturn(Arrays.asList(detail));

        List<StockOutDetails> result = stockOutService.getDetailsByStockId(1L);

        assertEquals(1, result.size());
        verify(stockOutDao, times(1)).getDetailsByStockId(1L);
    }

    @Test
    void testGetDetailsByHeaderId_InvalidId_ThrowsException() {
        assertThrows(BadRequestException.class,
                () -> stockOutService.getDetailsByStockId(null));
        assertThrows(BadRequestException.class,
                () -> stockOutService.getDetailsByStockId(0L));
        verify(stockOutDao, never()).getDetailsByStockId(any());
    }

    @Test
    void testGetStockOutHeaders_Success() {
        when(stockOutDao.getStockOutHeaders()).thenReturn(Arrays.asList(header));

        List<StockOutHeader> result = stockOutService.getStockOutHeaders();

        assertEquals(1, result.size());
        verify(stockOutDao, times(1)).getStockOutHeaders();
    }
    
    @Test
    void testGetHeadersBySupplierId_Success() {
        when(stockOutDao.getHeadersBySupplierId(101L)).thenReturn(Arrays.asList(header));

        List<StockOutHeader> result = stockOutService.getHeadersBySupplierId(101L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(stockOutDao, times(1)).getHeadersBySupplierId(101L);
    }

    @Test
    void testGetHeadersBySupplierId_EmptyResult() {
        when(stockOutDao.getHeadersBySupplierId(999L)).thenReturn(Collections.emptyList());

        List<StockOutHeader> result = stockOutService.getHeadersBySupplierId(999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(stockOutDao, times(1)).getHeadersBySupplierId(999L);
    }

    @Test
    void testGetHeadersByProformaId_Success() {
        when(stockOutDao.getHeadersByProformaId(303L)).thenReturn(Arrays.asList(header));

        List<StockOutHeader> result = stockOutService.getHeadersByProformaId(303L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(stockOutDao, times(1)).getHeadersByProformaId(303L);
    }

    @Test
    void testGetHeadersByProformaId_EmptyResult() {
        when(stockOutDao.getHeadersByProformaId(888L)).thenReturn(Collections.emptyList());

        List<StockOutHeader> result = stockOutService.getHeadersByProformaId(888L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(stockOutDao, times(1)).getHeadersByProformaId(888L);
    }
}