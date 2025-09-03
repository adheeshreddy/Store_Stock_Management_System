package com.stockManagement.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.stockManagement.dao.interfaces.ProformaDao;
import com.stockManagement.dao.interfaces.PurchaseDao;
import com.stockManagement.dao.interfaces.PurchaseLookupDao;
import com.stockManagement.exception.InvalidDataException;
import com.stockManagement.exception.ProformaDeletedException;
import com.stockManagement.exception.ResourceNotFoundException;
import com.stockManagement.models.ProformaDetails;
import com.stockManagement.models.ProformaHeader;
import com.stockManagement.models.StockOutHeader;
import com.stockManagement.service.interfaces.StockOutService;
import com.stockManagement.service.interfaces.impl.ProformaServiceImpl;
import com.stockManagement.validation.ProformaValidator;

class ProformaServiceUnitTest {

	@Mock
	private ProformaDao proformaDao;
	@Mock
	private PurchaseLookupDao purchaseLookupDao;
	@Mock
	private PurchaseDao purchaseDao;
	@Mock
	private StockOutService stockOutService;
	@Mock
	private static ProformaValidator validator;

	@InjectMocks
	private ProformaServiceImpl service;

	@BeforeEach
	void init() {
		MockitoAnnotations.openMocks(this);
		doNothing().when(validator);
		ProformaValidator.validateDelete(any());
		doNothing().when(validator);
		ProformaValidator.validateCreateOrEdit(any());
		doNothing().when(validator);
		ProformaValidator.validateApprove(any());
	}

	private ProformaHeader sampleHeader(Long id) {
		ProformaHeader h = new ProformaHeader();
		h.setId(id);
		h.setSupplierId(1L);
		h.setCreatedBy("admin");
		h.setStatus("C");
		h.setCreatedAt(LocalDate.now());
		ProformaDetails d1 = new ProformaDetails(id, 101L, "P1001", 10, 100.0, 18.0, 118.0,
				LocalDate.now().plusDays(90), "C");
		ProformaDetails d2 = new ProformaDetails(id, 102L, "P2001", 5, 50.0, 9.0, 59.0, LocalDate.now().plusDays(60),
				"C");
		h.setProformaDetails(List.of(d1, d2));
		return h;
	}

	@Test
	void createProforma_success() {
		ProformaHeader in = sampleHeader(null);
		when(proformaDao.insertProforma(any())).thenReturn(1L);

		Long id = service.createProforma(in);

		assertThat(id).isEqualTo(1L);
		verify(purchaseDao, atLeastOnce()).updateQuantity(anyLong(), anyInt(), eq("increase"));
		verify(proformaDao).insertProforma(any());
	}

	@Test
	void editProforma_nullId_throws() {
		ProformaHeader in = sampleHeader(null);
		assertThatThrownBy(() -> service.editProforma(in)).isInstanceOf(InvalidDataException.class);
		verify(proformaDao, never()).updateProforma(any());
	}

	@Test
	void editProforma_notFound_throws() {
		ProformaHeader in = sampleHeader(9L);
		when(proformaDao.findById(9L, true)).thenReturn(null);

		assertThatThrownBy(() -> service.editProforma(in)).isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("Proforma not found");
	}

	@Test
	void editProforma_success() {
		ProformaHeader in = sampleHeader(1L);
		when(proformaDao.findById(1L, true)).thenReturn(in);
		when(proformaDao.updateProforma(any())).thenReturn(1L);

		Long out = service.editProforma(in);

		assertThat(out).isEqualTo(1L);
		verify(purchaseDao, atLeastOnce()).updateQuantity(anyLong(), anyInt(), anyString());
		verify(proformaDao).updateProforma(in);
	}

	@Test
	void deleteProforma_success() {
		ProformaHeader in = sampleHeader(1L);
		when(proformaDao.findById(1L, true)).thenReturn(in);
		when(proformaDao.softDelete(1L, "admin")).thenReturn(1L);

		Long out = service.deleteProforma(in);

		verify(purchaseDao, atLeastOnce()).updateQuantity(anyLong(), anyInt(), eq("decrease"));
		assertThat(out).isEqualTo(1L);
		verify(proformaDao).softDelete(1L, "admin");
	}

	@Test
	void deleteProforma_notFound_throws() {
		ProformaHeader in = sampleHeader(1L);
		when(proformaDao.findById(1L, true)).thenReturn(null);

		assertThatThrownBy(() -> service.deleteProforma(in)).isInstanceOf(InvalidDataException.class)
				.hasMessageContaining("Proforma not found");

		verify(proformaDao).findById(1L, true);
		verify(proformaDao, never()).softDelete(anyLong(), anyString());
	}

	@Test
	void approveProforma_success_createsStockOuts() {
		ProformaHeader stored = sampleHeader(1L);
		when(proformaDao.findById(1L, true)).thenReturn(stored);
		when(proformaDao.approveProforma(1L)).thenReturn(stored);
		when(purchaseLookupDao.existsBySupplierAndBatch(anyLong(), anyLong())).thenReturn(true);

		ProformaHeader req = new ProformaHeader();
		req.setId(1L);
		req.setCreatedBy("admin");

		ProformaHeader out = service.approveProforma(req);

		assertThat(out.getId()).isEqualTo(1L);
		verify(stockOutService, atLeastOnce()).createStockOut(any(StockOutHeader.class));
	}

	@Test
	void approveProforma_deleted_throws() {
		ProformaHeader stored = sampleHeader(1L);
		stored.setStatus("D");
		when(proformaDao.findById(1L, true)).thenReturn(stored);

		ProformaHeader req = new ProformaHeader();
		req.setId(1L);

		assertThatThrownBy(() -> service.approveProforma(req)).isInstanceOf(ProformaDeletedException.class);
	}

	@Test
    void approveProforma_notFound_throws() {
        when(proformaDao.findById(5L, true)).thenReturn(null);
        ProformaHeader req = new ProformaHeader();
        req.setId(5L);

        assertThatThrownBy(() -> service.approveProforma(req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

	@Test
	void getProformaById_success() {
		ProformaHeader h = sampleHeader(2L);
		when(proformaDao.findById(2L, true)).thenReturn(h);

		ProformaHeader out = service.getProformaById(2L, true);

		assertThat(out.getId()).isEqualTo(2L);
	}

	@Test
    void getProformaById_notFound_throws() {
        when(proformaDao.findById(10L, true)).thenReturn(null);

        assertThatThrownBy(() -> service.getProformaById(10L, true))
                .isInstanceOf(InvalidDataException.class);
    }

	@Test
    void getAllProformas_delegates() {
        when(proformaDao.findAll(true)).thenReturn(List.of(sampleHeader(1L)));

        var list = service.getAllProformas(true);

        assertThat(list).hasSize(1);
        verify(proformaDao).findAll(true);
    }
}