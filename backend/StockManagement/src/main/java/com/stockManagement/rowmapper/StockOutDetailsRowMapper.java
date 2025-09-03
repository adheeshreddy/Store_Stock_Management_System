package com.stockManagement.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import com.stockManagement.models.StockOutDetails;

public class StockOutDetailsRowMapper implements RowMapper<StockOutDetails> {

    @Override
    public StockOutDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
        StockOutDetails detail = new StockOutDetails();
        detail.setStockOutId(rs.getLong("id"));
        detail.setHeaderId(rs.getLong("headerId"));
        detail.setBatchId(rs.getLong("batchId"));
        detail.setProductId(rs.getString("productId"));
        detail.setQuantity(rs.getInt("quantity"));
        detail.setTaxableAmount(rs.getDouble("taxableAmount"));
        detail.setGstAmount(rs.getDouble("gstAmount"));
        detail.setTotalAmount(rs.getDouble("totalAmount"));
        
        detail.setExpiry(rs.getDate("expiry") != null ? rs.getDate("expiry").toLocalDate() : null);
        return detail;
    }
}