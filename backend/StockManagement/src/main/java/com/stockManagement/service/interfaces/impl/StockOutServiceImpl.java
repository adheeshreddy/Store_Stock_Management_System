package com.stockManagement.service.interfaces.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stockManagement.dao.interfaces.StockOutDao;
import com.stockManagement.exception.InvalidDataException;
import com.stockManagement.models.StockOutDetails;
import com.stockManagement.models.StockOutHeader;
import com.stockManagement.service.interfaces.StockOutService;
import com.stockManagement.validation.StockOutValidator;

@Service
public class StockOutServiceImpl implements StockOutService {

	@Autowired
	private StockOutDao stockOutDao;

	@Override
	@Transactional
	public StockOutHeader createStockOut(StockOutHeader header) {
		StockOutValidator.validateForCreate(header);
		return stockOutDao.createStockOut(header);
	}

	@Override
	@Transactional
	public List<StockOutDetails> getDetailsByStockId(Long headerId) {
		if (headerId == null || headerId <= 0) {
			throw new InvalidDataException("Invalid headerId");
		}
		return stockOutDao.getDetailsByStockId(headerId);
	}

	@Override
	@Transactional
	public List<StockOutHeader> getStockOutHeaders() {
		return stockOutDao.getStockOutHeaders();
	}

	@Override
	@Transactional
	public List<StockOutHeader> getHeadersBySupplierId(Long headerId) {
		return stockOutDao.getHeadersBySupplierId(headerId);
	}
	
	@Override
	@Transactional
	public List<StockOutHeader> getHeadersByProformaId(Long headerId) {
		return stockOutDao.getHeadersByProformaId(headerId);
	}
}
