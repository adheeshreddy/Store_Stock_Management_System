package com.stockManagement.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.stockManagement.exception.InvalidDataException;
import com.stockManagement.models.PurchaseDetails;
import com.stockManagement.models.PurchaseHeader;
import com.stockManagement.service.interfaces.PurchaseService;

@SpringBootTest
@Transactional
public class PurchaseServiceIntegrationTest {

	@Autowired
	private PurchaseService purchaseService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void testCreatePurhcaseAndRollbackOnInvalidDetails() {

		int before = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tbl_PurchaseHeader", Integer.class);
		System.out.println(before);
		PurchaseHeader header = new PurchaseHeader();
		header.setSupplierId(5L);
		header.setTotalTaxableAmount(1000.00);
		header.setTotalGst(100.0);
		header.setTotalAmount(1100.0);
		header.setCreatedBy("admin");
		header.setCreatedAt(LocalDate.now());
		PurchaseDetails detail = new PurchaseDetails();
		detail.setProductId(null);
		detail.setQuantity(100);
		detail.setTaxableAmount(10.0);
		detail.setGstAmount(1.0);
		detail.setExpiry(LocalDate.now().plusDays(7));
		detail.setTotalAmount(11.0);
		header.setPurchaseDetails(List.of(detail));
		assertThrows(InvalidDataException.class, () -> purchaseService.createPurchase(header));
		int after = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tbl_PurchaseHeader", Integer.class);
		System.out.println(after);
		assertEquals(before, after);

	}

	@Test
	void testCreatePurhcaseValidDetails() {

		int before = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tbl_PurchaseHeader", Integer.class);
		System.out.println(before);
		PurchaseHeader header = new PurchaseHeader();
		header.setSupplierId(5L);
		header.setTotalTaxableAmount(1000.00);
		header.setTotalGst(100.0);
		header.setTotalAmount(1100.0);
		header.setCreatedBy("admin");
		header.setCreatedAt(LocalDate.now());
		PurchaseDetails detail = new PurchaseDetails();
		detail.setProductId("P1001");
		detail.setQuantity(100);
		detail.setTaxableAmount(10.0);
		detail.setGstAmount(1.0);
		detail.setTotalAmount(11.0);
		detail.setExpiry(LocalDate.now().plusDays(7));
		header.setPurchaseDetails(List.of(detail));
		purchaseService.createPurchase(header);
		// assertThrows(InvalidDataException.class,()->
		// purchaseService.createPurchase(header));
		int after = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tbl_PurchaseHeader", Integer.class);
		System.out.println(after);
		assertEquals(before + 1, after);

	}

	@Test
	void testCreatePurchaseAndRollbackOnException() {
		PurchaseHeader header = new PurchaseHeader();
		header.setId(200L);
		header.setSupplierId(9999L);

		try {
			purchaseService.createPurchase(header);
			throw new RuntimeException("Simulated failure after insert");
		} catch (Exception ignored) {
		}

		List<PurchaseHeader> headers = purchaseService.getPurchaseHeaders();
		boolean exists = headers.stream().anyMatch(h -> h.getId().equals(200L));
		assertFalse(exists);
	}

	@Test
	void testUpdateQuantityRollbackOnBadRequest() {
		assertThrows(InvalidDataException.class, () -> purchaseService.updateQuantity(null, 5, "increase"));
	}

	@Test
	void testGetDetailsByProductId() {
		List<PurchaseDetails> details = purchaseService.getDetails("P101");
		assertNotNull(details);
	}
}