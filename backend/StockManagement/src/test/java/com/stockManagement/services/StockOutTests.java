//package com.stockManagement.services;
//
//import com.stockManagement.dao.interfaces.StockOutDao;
//import com.stockManagement.exception.BadRequestException;
//import com.stockManagement.models.StockOutDetails;
//import com.stockManagement.models.StockOutHeader;
//import com.stockManagement.service.interfaces.impl.StockOutServiceImpl;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//public class StockOutTests {
//
//    @Mock
//    private StockOutDao stockOutDao;
//
//    @InjectMocks
//    private StockOutServiceImpl stockOutService;
//
//    private StockOutHeader header;
//    private StockOutDetails detail;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//
//        detail = new StockOutDetails();
//        detail.setHeaderId(101L);
//        detail.setProductId("P101");
//
//        header = new StockOutHeader();
//        header.setHeaderId(1L);
//        header.setStockOutDetails(Arrays.asList(detail));
//    }
//
//    @Test
//    void testCreateStockOut_Success() {
//        when(stockOutDao.createStockOut(header)).thenReturn(header.getHeaderId());
//
//        Long result = stockOutService.createStockOut(header);
//
//        assertNotNull(result);
//        assertEquals(1L, result);
//        verify(stockOutDao, times(1)).createStockOut(header);
//    }
//
//    @Test
//    void testCreateStockOut_NullHeader_ThrowsException() {
//        assertThrows(BadRequestException.class,
//                () -> stockOutService.createStockOut(null));
//        verify(stockOutDao, never()).createStockOut(any());
//    }
//
//    @Test
//    void testCreateStockOut_EmptyDetails_ThrowsException() {
//        header.setStockOutDetails(Collections.emptyList());
//
//        assertThrows(BadRequestException.class,
//                () -> stockOutService.createStockOut(header));
//        verify(stockOutDao, never()).createStockOut(any());
//    }
//
//    @Test
//    void testCreateStockOut_NullDetails_ThrowsException() {
//        header.setStockOutDetails(null);
//
//        assertThrows(BadRequestException.class,
//                () -> stockOutService.createStockOut(header));
//        verify(stockOutDao, never()).createStockOut(any());
//    }
//
//    @Test
//    void testGetDetailsByHeaderId_Success() {
//        when(stockOutDao.getDetailsByHeaderId(1L)).thenReturn(Arrays.asList(detail));
//
//        List<StockOutDetails> result = stockOutService.getDetailsByHeaderId(1L);
//
//        assertEquals(1, result.size());
//        verify(stockOutDao, times(1)).getDetailsByHeaderId(1L);
//    }
//
//    @Test
//    void testGetDetailsByHeaderId_InvalidId_ThrowsException() {
//        assertThrows(BadRequestException.class,
//                () -> stockOutService.getDetailsByHeaderId(null));
//        assertThrows(BadRequestException.class,
//                () -> stockOutService.getDetailsByHeaderId(0L));
//        verify(stockOutDao, never()).getDetailsByHeaderId(any());
//    }
//
//    @Test
//    void testGetStockOutHeaders_Success() {
//        when(stockOutDao.getStockOutHeaders()).thenReturn(Arrays.asList(header));
//
//        List<StockOutHeader> result = stockOutService.getStockOutHeaders();
//
//        assertEquals(1, result.size());
//        verify(stockOutDao, times(1)).getStockOutHeaders();
//    }
//}