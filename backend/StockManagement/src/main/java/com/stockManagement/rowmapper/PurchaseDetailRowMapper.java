package com.stockManagement.rowmapper;

import com.stockManagement.models.PurchaseDetails;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PurchaseDetailRowMapper implements RowMapper<PurchaseDetails> {

    @Override
    public PurchaseDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
        PurchaseDetails detail = new PurchaseDetails();
        detail.setHeaderId(rs.getLong("headerId"));
        detail.setBatchId(rs.getLong("batchId"));
        detail.setProductId(rs.getString("productId"));
        detail.setQuantity(rs.getInt("quantity"));
        detail.setTaxableAmount(rs.getDouble("taxableAmount"));
        detail.setGstAmount(rs.getDouble("gstAmount"));
        detail.setTotalAmount(rs.getDouble("totalAmount"));
        detail.setExpiry(rs.getDate("expiry").toLocalDate());
        detail.setReservedQuantity(rs.getInt("reservedQuantity"));
        detail.setAvailableQuantity(rs.getInt("availableQuantity"));
        return detail;
    }
}
