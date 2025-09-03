package com.stockManagement.service.interfaces.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stockManagement.dao.interfaces.ProformaDao;
import com.stockManagement.dao.interfaces.PurchaseDao;
import com.stockManagement.dao.interfaces.PurchaseLookupDao;
import com.stockManagement.exception.InvalidDataException;
import com.stockManagement.exception.ProformaDeletedException;
import com.stockManagement.exception.ResourceNotFoundException;
import com.stockManagement.models.ProformaDetails;
import com.stockManagement.models.ProformaHeader;
import com.stockManagement.models.StockOutDetails;
import com.stockManagement.models.StockOutHeader;
import com.stockManagement.service.interfaces.ProformaService;
import com.stockManagement.service.interfaces.StockOutService;
import com.stockManagement.validation.ProformaValidator;

@Service
public class ProformaServiceImpl implements ProformaService {

	private final ProformaDao proformaDao;
	private final PurchaseLookupDao purchaseLookupDao;
	private final PurchaseDao purchaseStockDao;
	private final StockOutService stockOutService;

	public ProformaServiceImpl(ProformaDao proformaDao, PurchaseLookupDao purchaseLookupDao,
			PurchaseDao purchaseStockDao, StockOutService stockOutService, ProformaValidator validator) {
		this.proformaDao = proformaDao;
		this.purchaseLookupDao = purchaseLookupDao;
		this.purchaseStockDao = purchaseStockDao;
		this.stockOutService = stockOutService;
	}

	@Override
	@Transactional
	public Long createProforma(ProformaHeader header) {

		ProformaValidator.validateCreateOrEdit(header);
		header.setCreatedAt(header.getCreatedAt() == null ? LocalDate.now() : header.getCreatedAt());
		header.setStatus("C");

		if (header.getProformaDetails() != null) {
			for (ProformaDetails d : header.getProformaDetails()) {
				d.setStatus("C");
				purchaseStockDao.updateQuantity(d.getBatchId(), d.getQuantity(), "increase");
			}
		}
		System.out.println(header);
		return proformaDao.insertProforma(header);
	}

	@Override
	@Transactional
	public Long editProforma(ProformaHeader header) {
		ProformaValidator.validateCreateOrEdit(header);
		if (header.getId() == null)
			throw new InvalidDataException("id required");
		header.setStatus("E");
		ProformaHeader existing = proformaDao.findById(header.getId(), true);
		System.out.println(existing);
		if (existing == null)
			throw new InvalidDataException("Proforma not found: " + header.getId());
		if (existing.getProformaDetails() != null) {
			for (ProformaDetails d : existing.getProformaDetails()) {
				purchaseStockDao.updateQuantity(d.getBatchId(), d.getQuantity(), "decrease");
			}
		}

		if (header.getProformaDetails() != null) {
			for (ProformaDetails d : header.getProformaDetails()) {
				purchaseStockDao.updateQuantity(d.getBatchId(), d.getQuantity(), "increase");
			}
		}

		return proformaDao.updateProforma(header);
	}

	@Override
	@Transactional
	public Long deleteProforma(ProformaHeader header) {

		ProformaValidator.validateDelete(header);
		ProformaHeader existing = proformaDao.findById(header.getId(), true);
		if (existing == null)
			throw new InvalidDataException("Proforma not found: " + header.getId());

		if (existing.getProformaDetails() != null) {
			for (ProformaDetails d : existing.getProformaDetails()) {
				purchaseStockDao.updateQuantity(d.getBatchId(), d.getQuantity(), "decrease");
			}
		}

		return proformaDao.softDelete(header.getId(), header.getCreatedBy());
	}

	@Override
	@Transactional
	public ProformaHeader approveProforma(ProformaHeader header) {
		ProformaValidator.validateApprove(header);

		ProformaHeader full = proformaDao.findById(header.getId(), true);
		if (full == null)
			throw new ResourceNotFoundException("Proforma not found with id: " + header.getId());
		if (full.getStatus() != null && full.getStatus() == "D")
			throw new ProformaDeletedException("Cannot approve deleted proforma : " + header.getId());

		ProformaHeader approved = proformaDao.approveProforma(full.getId());

		List<StockOutDetails> invoiceLines = new ArrayList<>();
		List<StockOutDetails> returnLines = new ArrayList<>();

		if (full.getProformaDetails() != null) {
			for (ProformaDetails d : full.getProformaDetails()) {
				boolean belongs = purchaseLookupDao.existsBySupplierAndBatch(full.getSupplierId(), d.getBatchId());
				StockOutDetails s = new StockOutDetails();
				s.setHeaderId(approved.getId());
				s.setBatchId(d.getBatchId());
				s.setProductId(d.getProductId());
				s.setQuantity(d.getQuantity());
				s.setTaxableAmount(d.getTaxableAmount());
				s.setGstAmount(d.getGstAmount());
				s.setTotalAmount(d.getTotalAmount());
				s.setExpiry(d.getExpiry());

				if (belongs) {
					returnLines.add(s);
				} else {
					invoiceLines.add(s);
				}
			}
		}

		java.util.function.BiFunction<Character, List<StockOutDetails>, StockOutHeader> buildHeader = (status,
				lines) -> {
			double totalTaxable = 0, totalGst = 0, totalAmount = 0;
			for (StockOutDetails x : lines) {
				totalTaxable += (x.getTaxableAmount() != null ? x.getTaxableAmount() : 0);
				totalGst += (x.getGstAmount() != null ? x.getGstAmount() : 0);
				totalAmount += (x.getTotalAmount() != null ? x.getTotalAmount() : 0);
			}
			StockOutHeader h = new StockOutHeader();
			h.setHeaderId(approved.getId()); 
			h.setSupplierId(approved.getSupplierId());
			h.setStatus(status); 
			h.setTotalTaxableAmount(totalTaxable);
			h.setTotalGst(totalGst);
			h.setTotalAmount(totalAmount);
			h.setApprovedBy(header.getCreatedBy() != null ? header.getCreatedBy() : "admin");
			h.setApprovedAt(LocalDateTime.now());
			h.setStockOutDetails(lines);
			for (StockOutDetails d : h.getStockOutDetails()) {
				purchaseStockDao.updateQuantity(d.getBatchId(), d.getQuantity(), "approved");
			}
			return h;
		};

		if (!invoiceLines.isEmpty()) {
			StockOutHeader soI = buildHeader.apply('I', invoiceLines);
			stockOutService.createStockOut(soI);
		}
		if (!returnLines.isEmpty()) {
			StockOutHeader soR = buildHeader.apply('R', returnLines);
			stockOutService.createStockOut(soR);
		}

		return approved;
	}

	@Override
	public List<ProformaHeader> getAllProformas(Boolean details) {
		return proformaDao.findAll(Boolean.TRUE.equals(details));
	}

	private void recomputeTotals(ProformaHeader h) {
		if (h.getProformaDetails() == null || h.getProformaDetails().isEmpty()) {
			h.setTotalTaxableAmount(0.0);
			h.setTotalGst(0.0);
			h.setTotalAmount(0.0);
			return;
		}

		double tt = 0, tg = 0, ta = 0;
		for (ProformaDetails d : h.getProformaDetails()) {
			tt += d.getTaxableAmount();
			tg += d.getGstAmount();
			ta += d.getTotalAmount();
		}
		h.setTotalTaxableAmount(tt);
		h.setTotalGst(tg);
		h.setTotalAmount(ta);
	}

	@Override
	public ProformaHeader getProformaById(Long id, boolean details) {
		ProformaHeader header = proformaDao.findById(id, details);
		if (header == null) {
			throw new InvalidDataException("Proforma not found: " + id);
		}
		return header;
	}

}
