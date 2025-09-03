package com.stockManagement.dao.interfaces.impl;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.stockManagement.dao.interfaces.ProformaDao;
import com.stockManagement.models.ProformaDetails;
import com.stockManagement.models.ProformaHeader;

@Repository
public class ProformaDaoImpl implements ProformaDao {

	private final JdbcTemplate jdbc;

	private static final String HDR_COLS = "id, supplierId, totalTaxableAmount, totalGst, totalAmount, status, createdAt, createdBy";
	private static final String DTL_COLS = "headerId, batchId, productId, quantity, taxableAmount, gstAmount, totalAmount, expiry, status";

	public ProformaDaoImpl(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	private final RowMapper<ProformaHeader> headerMapper = (rs, n) -> {
		ProformaHeader header = new ProformaHeader();
		header.setId(rs.getLong("id"));
		header.setSupplierId(rs.getLong("supplierId"));
		header.setTotalTaxableAmount(rs.getDouble("totalTaxableAmount"));
		header.setTotalGst(rs.getDouble("totalGst"));
		header.setTotalAmount(rs.getDouble("totalAmount"));
		header.setStatus(rs.getString("status"));
		Date d = rs.getDate("createdAt");
		header.setCreatedAt(d != null ? d.toLocalDate() : null);
		header.setCreatedBy(rs.getString("createdBy"));
		return header;
	};

	private final RowMapper<ProformaDetails> detailsMapper = (rs, n) -> {
		ProformaDetails details = new ProformaDetails();
		details.setHeaderId(rs.getLong("headerId"));
		details.setBatchId(rs.getLong("batchId"));
		details.setProductId(rs.getString("productId"));
		details.setQuantity(rs.getInt("quantity"));
		details.setTaxableAmount(rs.getDouble("taxableAmount"));
		details.setGstAmount(rs.getDouble("gstAmount"));
		details.setTotalAmount(rs.getDouble("totalAmount"));
		details.setExpiry(rs.getDate("expiry").toLocalDate());
		details.setStatus(rs.getString("status"));
		return details;
	};

	private Long logHeaderSnapshot(Long headerId, char editType, String byUser) {
		final String sql = "INSERT INTO tbl_ProformaLogHeader "
				+ "(headerId,supplierId,totalTaxableAmount,totalGst,totalAmount,editType,changedBy) "
				+ "SELECT id,supplierId,totalTaxableAmount,totalGst,totalAmount,?,? FROM tbl_ProformaHeader WHERE id=?";
		KeyHolder kh = new GeneratedKeyHolder();
		jdbc.update(con -> {
			PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, String.valueOf(editType));
			ps.setString(2, byUser != null ? byUser : "system");
			ps.setLong(3, headerId);
			return ps;
		}, kh);
		Number key = kh.getKey();
		return key != null ? key.longValue() : jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
	}

	private void logDetailsSnapshot(Long logId, Long headerId, char statusForLog) {
		final String sql = "INSERT INTO tbl_ProformaLogDetails "
				+ "(logId,headerId,batchId,productId,quantity,taxableAmount,gstAmount,totalAmount,expiry,status) "
				+ "SELECT ?,headerId,batchId,productId,quantity,taxableAmount,gstAmount,totalAmount,expiry,? "
				+ "FROM tbl_ProformaDetails WHERE headerId=?";
		jdbc.update(sql, logId, String.valueOf(statusForLog), headerId);
	}

	private List<ProformaDetails> selectDetails(Long headerId) {
		return jdbc.query("SELECT " + DTL_COLS + " FROM tbl_ProformaDetails WHERE headerId=?", detailsMapper, headerId);
	}

	private void insertDetails(Long headerId, List<ProformaDetails> dets) {
		if (dets == null || dets.isEmpty())
			return;

		final String sql = "INSERT INTO tbl_ProformaDetails "
				+ "(headerId, batchId, productId, quantity, taxableAmount, gstAmount, totalAmount, expiry, status) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

		for (ProformaDetails details : dets) {
			jdbc.update(sql, headerId, details.getBatchId(), details.getProductId(), details.getQuantity(), details.getTaxableAmount(),
					details.getGstAmount(), details.getTotalAmount(), details.getExpiry() != null ? Date.valueOf(details.getExpiry()) : null,
					details.getStatus());
		}
	}

	@Override
	public Long insertProforma(ProformaHeader header) {
		if (header.getCreatedAt() == null)
			header.setCreatedAt(LocalDate.now());
		if (header.getStatus() == null)
			header.setStatus("C");

		final String sql = "INSERT INTO tbl_ProformaHeader(supplierId,totalTaxableAmount,totalGst,totalAmount,status,createdAt,createdBy) "
				+ "VALUES (?,?,?,?,?,?,?)";
		KeyHolder kh = new GeneratedKeyHolder();
		jdbc.update(con -> {
			PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setLong(1, header.getSupplierId());
			ps.setDouble(2, header.getTotalTaxableAmount());
			ps.setDouble(3, header.getTotalGst());
			ps.setDouble(4, header.getTotalAmount());
			ps.setString(5, String.valueOf(header.getStatus().charAt(0)));
			ps.setDate(6, Date.valueOf(header.getCreatedAt()));
			ps.setString(7, header.getCreatedBy());
			return ps;
		}, kh);
		header.setId(kh.getKey().longValue());

		insertDetails(header.getId(), header.getProformaDetails());
		return header.getId();
	}

	@Override
	public Long updateProforma(ProformaHeader header) {

		Long logId = logHeaderSnapshot(header.getId(), 'E', header.getCreatedBy());
		logDetailsSnapshot(logId, header.getId(), 'E');
		jdbc.update("UPDATE tbl_ProformaDetails SET status='D' WHERE headerId=?", header.getId());
		final String sql = "UPDATE tbl_ProformaHeader "
				+ "SET supplierId=?, totalTaxableAmount=?, totalGst=?, totalAmount=?, status=? " + "WHERE id=?";
		jdbc.update(sql, header.getSupplierId(), header.getTotalTaxableAmount(), header.getTotalGst(), header.getTotalAmount(),
				String.valueOf(header.getStatus().charAt(0)), header.getId());
		jdbc.update("DELETE FROM tbl_ProformaDetails WHERE headerId=?", header.getId());
		insertDetails(header.getId(), header.getProformaDetails());

		return header.getId();
	}

	@Override
	public Long softDelete(Long headerId, String byUser) {

		Long logId = logHeaderSnapshot(headerId, 'D', byUser);
		logDetailsSnapshot(logId, headerId, 'D');

		jdbc.update("UPDATE tbl_ProformaHeader SET status='D' WHERE id=?", headerId);
		jdbc.update("UPDATE tbl_ProformaDetails SET status='D' WHERE headerId=?", headerId);
		return headerId;
	}

	@Override
	public ProformaHeader approveProforma(Long headerId) {
		jdbc.update("UPDATE tbl_ProformaHeader SET status='A' WHERE id=?", headerId);
		return findById(headerId, true);
	}

	@Override
	public ProformaHeader findById(Long id, boolean withDetails) {
		List<ProformaHeader> hs = jdbc.query(
				"SELECT id, supplierId, totalTaxableAmount, totalGst, totalAmount, status, createdAt, createdBy FROM tbl_ProformaHeader WHERE id=?",
				headerMapper, id);
		if (hs.isEmpty())
			return null;
		ProformaHeader header = hs.get(0);
		if (withDetails)
			header.setProformaDetails(selectDetails(id));
		return header;
	}

	@Override
	public List<ProformaHeader> findAll(boolean withDetails) {
		List<ProformaHeader> hs = jdbc.query("SELECT " + HDR_COLS + " FROM tbl_ProformaHeader ", headerMapper);
		if (!withDetails)
			return hs;
		for (ProformaHeader header : hs) {
			header.setProformaDetails(selectDetails(header.getId()));
		}
		return hs;
	}
}
