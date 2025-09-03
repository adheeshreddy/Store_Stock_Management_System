package com.stockManagement.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import com.stockManagement.models.StockOutHeader;

public class StockOutHeaderRowMapper implements RowMapper<StockOutHeader> {

    @Override
    public StockOutHeader mapRow(ResultSet rs, int rowNum) throws SQLException {
        StockOutHeader header = new StockOutHeader();
        header.setId(rs.getLong("id"));
        header.setHeaderId(rs.getLong("headerId"));
        header.setSupplierId(rs.getLong("supplierId"));
        String st = rs.getString("status");
        header.setStatus(st != null && !st.isEmpty() ? st.charAt(0) : null);
        header.setTotalTaxableAmount(rs.getDouble("totalTaxableAmount"));
        header.setTotalGst(rs.getDouble("totalGst"));
        header.setTotalAmount(rs.getDouble("totalAmount"));
        header.setApprovedAt(rs.getTimestamp("approvedAt") != null ? rs.getTimestamp("approvedAt").toLocalDateTime() : null);
        header.setApprovedBy(rs.getString("approvedBy"));
        return header;
    }
}
