package com.stockManagement.dao.interfaces.impl;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.stockManagement.dao.interfaces.StockOutDao;
import com.stockManagement.models.StockOutDetails;
import com.stockManagement.models.StockOutHeader;
import com.stockManagement.rowmapper.StockOutDetailsRowMapper;
import com.stockManagement.rowmapper.StockOutHeaderRowMapper;

@Repository
public class StockOutDaoImpl implements StockOutDao {

	private JdbcTemplate template;

	@Autowired
	public void setTemplate(JdbcTemplate template) {
		this.template = template;
	}

	@Override
	public StockOutHeader createStockOut(StockOutHeader header) {
		final String hdrSql = "INSERT INTO tbl_StockOutHeader "
				+ "(headerId, supplierId, status, totalTaxableAmount, totalGst, totalAmount, approvedAt, approvedBy) "
				+ "VALUES (?,?,?,?,?,?,?,?)";

		KeyHolder kh = new GeneratedKeyHolder();

		template.update(con -> {
			PreparedStatement ps = con.prepareStatement(hdrSql, Statement.RETURN_GENERATED_KEYS);
			ps.setLong(1, header.getHeaderId());
			ps.setLong(2, header.getSupplierId());
			ps.setString(3, String.valueOf(header.getStatus())); // 'I' or 'R'
			ps.setDouble(4, header.getTotalTaxableAmount());
			ps.setDouble(5, header.getTotalGst());
			ps.setDouble(6, header.getTotalAmount());
			ps.setObject(7, header.getApprovedAt());
			ps.setString(8, header.getApprovedBy());
			return ps;
		}, kh);

		Long stockOutId = kh.getKey().longValue();
		header.setId(stockOutId);

		// details
		final String detSql = "INSERT INTO tbl_StockOutDetails "
				+ "(id, headerId, batchId, productId, quantity, taxableAmount, gstAmount, totalAmount, expiry) "
				+ "VALUES (?,?,?,?,?,?,?,?,?)";

		if (header.getStockOutDetails() != null) {
			for (StockOutDetails d : header.getStockOutDetails()) {
				d.setStockOutId(stockOutId); // FK to header PK
				d.setHeaderId(header.getHeaderId()); // ProformaHeader.id

				Date expiry = null;
				if (d.getExpiry() != null) {
					LocalDate ex = d.getExpiry();
					expiry = Date.valueOf(ex);
				}

				template.update(detSql, d.getStockOutId(), d.getHeaderId(), d.getBatchId(), d.getProductId(),
						d.getQuantity(), d.getTaxableAmount(), d.getGstAmount(), d.getTotalAmount(), expiry);
			}
		}

		return header;
	}

	@Override
	public List<StockOutDetails> getDetailsByStockId(Long headerId) {
		String sql = "SELECT id, headerId, batchId, productId, quantity, taxableAmount, gstAmount, totalAmount, expiry "
				+ "FROM tbl_StockOutDetails WHERE id = ?";
		return template.query(sql, new StockOutDetailsRowMapper(), headerId);
	}

	@Override
	public List<StockOutHeader> getHeadersBySupplierId(Long headerId) {
		String sql = "SELECT id, headerId, supplierId, status, totalTaxableAmount, totalGst, totalAmount, approvedAt, approvedBy  "
				+ "FROM tbl_StockOutHeader WHERE supplierId = ?";
		return template.query(sql, new StockOutHeaderRowMapper(), headerId);
	}

	@Override
	public List<StockOutHeader> getStockOutHeaders() {
		String sql = "SELECT id, headerId, supplierId, status, totalTaxableAmount, totalGst, totalAmount, approvedAt, approvedBy "
				+ "FROM tbl_StockOutHeader ORDER BY id DESC";
		return template.query(sql, new StockOutHeaderRowMapper());
	}

	@Override
	public List<StockOutHeader> getHeadersByProformaId(Long headerId) {
		String sql = "SELECT id, headerId, supplierId, status, totalTaxableAmount, totalGst, totalAmount, approvedAt, approvedBy  "
				+ "FROM tbl_StockOutHeader WHERE headerId = ?";
		return template.query(sql, new StockOutHeaderRowMapper(), headerId);
	}
}
