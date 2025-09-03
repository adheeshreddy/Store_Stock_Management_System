package com.stockManagement.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.stockManagement.exception.InvalidDataException;
import com.stockManagement.models.ProformaDetails;
import com.stockManagement.models.ProformaHeader;
import com.stockManagement.service.interfaces.ProformaService;

@SpringBootTest
@Transactional
public class ProformaServiceIntegrationTest {

	@Autowired
	private ProformaService proformaService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private double getReservedQuantity(Long batchId) {
		return jdbcTemplate.queryForObject("SELECT reservedQuantity FROM tbl_purchaseDetails WHERE batchId = ?",
				Double.class, batchId);
	}

	@Test
	void testCreateProforma_shouldIncreaseReservedQuantity() {
		Long batchId = 101L;
		double initialQty = getReservedQuantity(batchId);

		ProformaDetails detail = new ProformaDetails();
		detail.setBatchId(batchId);
		detail.setProductId("P1021121");
		detail.setExpiry(LocalDate.now().plusDays(7));
		detail.setQuantity(100);
		detail.setTaxableAmount(10.0);
		detail.setGstAmount(1.0);
		detail.setTotalAmount(110.0);
		detail.setQuantity(5);

		ProformaHeader header = new ProformaHeader();
		header.setSupplierId(1L);
		header.setCreatedBy("tester");
		header.setProformaDetails(Arrays.asList(detail));
		header.setTotalTaxableAmount(1000.0);
		header.setTotalGst(100.0);
		header.setTotalAmount(1100.0);
		Long id = proformaService.createProforma(header);

		double updatedQty = getReservedQuantity(batchId);
		assertThat(updatedQty).isEqualTo(initialQty + 5.0);

		int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tbl_proformaHeader WHERE id = ?", Integer.class,
				id);
		assertThat(count).isEqualTo(1);
	}

	@Test
	void testEditProforma_shouldRollbackOldAndApplyNewQuantities() {
		Long batchId = 102L;
		double initialQty = getReservedQuantity(batchId);

		ProformaDetails oldDetail = new ProformaDetails();
		oldDetail.setBatchId(batchId);
		oldDetail.setQuantity(3);
		oldDetail.setProductId("P222");
		oldDetail.setExpiry(LocalDate.now().plusDays(7));
		oldDetail.setQuantity(100);
		oldDetail.setTaxableAmount(10.0);
		oldDetail.setGstAmount(1.0);
		oldDetail.setTotalAmount(110.0);

		ProformaHeader header = new ProformaHeader();
		header.setSupplierId(1L);
		header.setCreatedBy("tester");
		header.setTotalTaxableAmount(1000.00);
		header.setTotalGst(100.0);
		header.setTotalAmount(1100.0);
		header.setProformaDetails(Arrays.asList(oldDetail));
		Long id = proformaService.createProforma(header);

		ProformaDetails newDetail = new ProformaDetails();
		newDetail.setBatchId(batchId);
		newDetail.setQuantity(7);
		newDetail.setProductId("P222");
		newDetail.setTaxableAmount(10.0);
		newDetail.setGstAmount(1.0);
		newDetail.setTotalAmount(110.0);
		newDetail.setExpiry(LocalDate.now().plusDays(7));

		ProformaHeader edited = new ProformaHeader();
		edited.setId(id);
		edited.setSupplierId(1L);
		edited.setCreatedBy("tester");
		edited.setTotalTaxableAmount(1000.00);
		edited.setTotalGst(100.0);
		edited.setTotalAmount(1100.0);
		edited.setProformaDetails(Arrays.asList(newDetail));

		proformaService.editProforma(edited);

		double finalQty = getReservedQuantity(batchId);
		assertThat(finalQty).isEqualTo(initialQty + 7.0); // 3 removed, 7 added
	}

	@Test
	void testCreateProforma_shouldRollbackOnException() {
		Long batchId = 101L;
		double initialQty = getReservedQuantity(batchId);

		ProformaDetails detail = new ProformaDetails();
		detail.setBatchId(batchId);
		detail.setProductId("P1001");
		detail.setTaxableAmount(10.0);
		detail.setGstAmount(1.0);
		detail.setExpiry(LocalDate.now().plusDays(7));
		detail.setTotalAmount(11.0);
		detail.setQuantity(-1);

		ProformaHeader header = new ProformaHeader();
		header.setSupplierId(1L);
		header.setCreatedBy("tester");
		header.setProformaDetails(Arrays.asList(detail));

		assertThrows(InvalidDataException.class, () -> proformaService.createProforma(header));

		double finalQty = getReservedQuantity(batchId);
		assertThat(finalQty).isEqualTo(initialQty);
	}

	@Test
	void testDeleteProforma_shouldRollbackReservedQuantity() {
		Long batchId = 101L;
		double initialQty = getReservedQuantity(batchId);

		ProformaDetails detail = new ProformaDetails();
		detail.setBatchId(batchId);
		detail.setProductId("P1001");
		detail.setTaxableAmount(10.0);
		detail.setGstAmount(1.0);
		detail.setExpiry(LocalDate.now().plusDays(7));
		detail.setTotalAmount(11.0);
		detail.setQuantity(1);

		ProformaHeader header = new ProformaHeader();
		header.setSupplierId(1L);
		header.setCreatedBy("tester");
		header.setTotalTaxableAmount(1000.0);
		header.setTotalGst(100.0);
		header.setTotalAmount(1100.0);
		header.setProformaDetails(Arrays.asList(detail));
		Long id= proformaService.createProforma(header);
		header.setId(id);
		proformaService.deleteProforma(header);

		double finalQty = getReservedQuantity(batchId);
		System.out.println(finalQty);
		assertThat(finalQty).isEqualTo(initialQty);
	}
}