package com.stockManagement.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.stockManagement.models.PurchaseHeader;

public class PurchaseHeaderRowMapper implements RowMapper<PurchaseHeader> {

    @Override
    public PurchaseHeader mapRow(ResultSet rs, int rowNum) throws SQLException {
    	 PurchaseHeader header = new PurchaseHeader();
         header.setId(rs.getLong("id"));
         header.setSupplierId(rs.getLong("supplierId"));
         header.setTotalTaxableAmount(rs.getDouble("totalTaxableAmount"));
         header.setTotalGst(rs.getDouble("totalGst"));
         header.setTotalAmount(rs.getDouble("totalAmount"));
         header.setCreatedAt(rs.getDate("createdAt").toLocalDate());
         header.setCreatedBy(rs.getString("createdBy"));
         return header;
    }
}
