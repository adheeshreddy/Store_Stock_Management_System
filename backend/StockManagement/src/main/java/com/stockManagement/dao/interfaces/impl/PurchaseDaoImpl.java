package com.stockManagement.dao.interfaces.impl;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.stockManagement.dao.interfaces.PurchaseDao;
import com.stockManagement.models.PurchaseDetails;
import com.stockManagement.models.PurchaseHeader;
import com.stockManagement.rowmapper.PurchaseDetailRowMapper;
import com.stockManagement.rowmapper.PurchaseHeaderRowMapper;

@Repository
public class PurchaseDaoImpl implements PurchaseDao {

	private JdbcTemplate template;

	@Autowired
	public void setTemplate(JdbcTemplate template) {
		this.template = template;
	}

	@Override
	public Long createPurchase(PurchaseHeader header) {
		String headerSql = "INSERT INTO tbl_purchaseHeader(supplierId, totalTaxableAmount, totalGst, totalAmount, createdBy, createdAt) VALUES (?, ?, ?, ?, ?, ?)";
		String detailsSql = "INSERT INTO tbl_purchaseDetails(headerId, productId, quantity, taxableAmount, gstAmount, totalAmount, expiry, reservedQuantity, availableQuantity) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		template.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(headerSql, Statement.RETURN_GENERATED_KEYS);
			ps.setLong(1, header.getSupplierId());
			ps.setDouble(2, header.getTotalTaxableAmount());
			ps.setDouble(3, header.getTotalGst());
			ps.setDouble(4, header.getTotalAmount());
			ps.setString(5, header.getCreatedBy());
			ps.setDate(6, Date.valueOf(header.getCreatedAt()));
			return ps;
		}, keyHolder);

		Long headerId = keyHolder.getKey().longValue();
		header.setId(headerId);

		List<Object[]> batchArgs = new ArrayList<>();
		for (PurchaseDetails detail : header.getPurchaseDetails()) {
			detail.setHeaderId(headerId);
			int quantity = detail.getQuantity();
			detail.setAvailableQuantity(quantity);
			detail.setReservedQuantity(0);

			Object[] args = { detail.getHeaderId(), detail.getProductId(), detail.getQuantity(),
					detail.getTaxableAmount(), detail.getGstAmount(), detail.getTotalAmount(),
					Date.valueOf(detail.getExpiry()), detail.getReservedQuantity(), detail.getAvailableQuantity() };
			batchArgs.add(args);
		}

		template.batchUpdate(detailsSql, batchArgs);
		return header.getId();
	}

	@Override
	public List<PurchaseDetails> getDetailsByHeaderId(Long headerId) {
		String sql = "SELECT headerId,batchId,productId,quantity,taxableAmount,gstAmount, totalAmount, expiry, reservedQuantity, availableQuantity FROM tbl_purchaseDetails WHERE headerId = ?";
		return template.query(sql, new PurchaseDetailRowMapper(), headerId);
	}

	@Override
	public List<PurchaseHeader> getPurchaseHeaders() {
	    String sql = "SELECT id, supplierId, totalTaxableAmount, totalGst, totalAmount, createdBy, createdAt FROM tbl_purchaseHeader";
		return template.query(sql, new PurchaseHeaderRowMapper());
	}

	@Override
	public List<PurchaseDetails> getDetailsByProductId(String productId) {
		String sql = "SELECT headerId,batchId,productId,quantity,taxableAmount,gstAmount, totalAmount, expiry, reservedQuantity, availableQuantity FROM tbl_purchaseDetails WHERE productId = ?";
		return template.query(sql, new PurchaseDetailRowMapper(), productId);
	}

	@Override
	public boolean updateQuantity(Long batchId, int reservedQuantity, String change) {
	    String sql;
	    if (change.equals("decrease")) {
	        sql = "UPDATE tbl_purchaseDetails SET reservedQuantity = reservedQuantity - ?, availableQuantity = availableQuantity + ? WHERE batchId = ?";
	    } else if(change.equals("increase")) {
	        sql = "UPDATE tbl_purchaseDetails SET reservedQuantity = reservedQuantity + ?, availableQuantity = availableQuantity - ? WHERE batchId = ?";
	    }else {
	        sql = "UPDATE tbl_purchaseDetails SET reservedQuantity = reservedQuantity - ? WHERE batchId = ?";
	        return template.update(sql, reservedQuantity , batchId) > 0;
	    }

	    return template.update(sql, reservedQuantity, reservedQuantity, batchId) > 0;
	}

	
	
}
