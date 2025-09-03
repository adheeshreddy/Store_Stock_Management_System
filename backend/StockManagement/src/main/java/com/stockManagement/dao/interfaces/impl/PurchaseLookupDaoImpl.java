package com.stockManagement.dao.interfaces.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.stockManagement.dao.interfaces.PurchaseLookupDao;

@Repository
public class PurchaseLookupDaoImpl implements PurchaseLookupDao {

	private final JdbcTemplate jdbc;

	public PurchaseLookupDaoImpl(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public boolean existsBySupplierAndBatch(Long supplierId, Long batchId) {
		final String sql = "SELECT COUNT(*) FROM tbl_PurchaseDetails d "
				+ "JOIN tbl_PurchaseHeader h ON h.id = d.headerId " + "WHERE h.supplierId=? AND d.batchId=?";
		Integer c = jdbc.queryForObject(sql, Integer.class, supplierId, batchId);
		return c != null && c > 0;
	}
}
