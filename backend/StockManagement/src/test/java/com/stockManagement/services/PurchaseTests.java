package com.stockManagement.services;

import com.stockManagement.dao.interfaces.PurchaseDao;
import com.stockManagement.exception.BadRequestException;
import com.stockManagement.models.PurchaseDetails;
import com.stockManagement.models.PurchaseHeader;
import com.stockManagement.service.interfaces.impl.PurchaseServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PurchaseTests {

	@Mock
	private PurchaseDao purchaseDao;

	@InjectMocks
	private PurchaseServiceImpl purchaseService;

	private PurchaseHeader header;
	private PurchaseDetails detail;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		header = new PurchaseHeader();
		header.setId(1L);
		header.setSupplierId(1023L);

		detail = new PurchaseDetails();
		detail.setBatchId(100L);
		detail.setHeaderId(1L);
		detail.setProductId("P101");
	}

	@Test
    void testCreatePurchase_Success() {
        when(purchaseDao.createPurchase(header)).thenReturn(header.getId());

        Long result = purchaseService.createPurchase(header);

        assertNotNull(result);
        assertEquals(1L, result);
        verify(purchaseDao, times(1)).createPurchase(header);
    }

	@Test
	void testCreatePurchase_Null_ThrowsException() {
		assertThrows(BadRequestException.class, () -> purchaseService.createPurchase(null));
		verify(purchaseDao, never()).createPurchase(any());
	}

	@Test
    void testGetDetails_ByHeaderId() {
        when(purchaseDao.getDetailsByHeaderId(1L)).thenReturn(Arrays.asList(detail));

        List<PurchaseDetails> result = purchaseService.getDetails("1");

        assertEquals(1, result.size());
        verify(purchaseDao, times(1)).getDetailsByHeaderId(1L);
    }

	@Test
    void testGetDetails_ByProductId() {
        when(purchaseDao.getDetailsByProductId("P101")).thenReturn(Arrays.asList(detail));

        List<PurchaseDetails> result = purchaseService.getDetails("P101");

        assertEquals(1, result.size());
        verify(purchaseDao, times(1)).getDetailsByProductId("P101");
    }

	@Test
    void testGetPurchaseHeaders() {
        when(purchaseDao.getPurchaseHeaders()).thenReturn(Arrays.asList(header));

        List<PurchaseHeader> result = purchaseService.getPurchaseHeaders();

        assertEquals(1, result.size());
        verify(purchaseDao, times(1)).getPurchaseHeaders();
    }

	@Test
    void testUpdateQuantity_Success() {
        when(purchaseDao.updateQuantity(100L, 5, "increase")).thenReturn(true);

        boolean result = purchaseService.updateQuantity(100L, 5, "increase");

        assertTrue(result);
        verify(purchaseDao, times(1)).updateQuantity(100L, 5, "increase");
    }

	@Test
	void testUpdateQuantity_InvalidBatchId_ThrowsException() {
		assertThrows(BadRequestException.class, () -> purchaseService.updateQuantity(null, 5, "increase"));
		verify(purchaseDao, never()).updateQuantity(any(), anyInt(), any());
	}

	@Test
	void testUpdateQuantity_InvalidReservedQuantity_ThrowsException() {
		assertThrows(BadRequestException.class, () -> purchaseService.updateQuantity(100L, -1, "increase"));
		verify(purchaseDao, never()).updateQuantity(any(), anyInt(), any());
	}
}
