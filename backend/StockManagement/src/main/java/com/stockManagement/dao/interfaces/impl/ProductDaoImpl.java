package com.stockManagement.dao.interfaces.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.stockManagement.dao.interfaces.ProductDao;
import com.stockManagement.models.Product;
import com.stockManagement.rowmapper.ProductRowMapper;

@Repository
public class ProductDaoImpl implements ProductDao {

    @Autowired
    private JdbcTemplate template;

    @Override
    public String saveProduct(Product product) {
        String sql = "INSERT INTO tbl_product(id, name, status) VALUES (?, ?, ?)";
        template.update(sql, product.getId(), product.getName(), product.getStatus());
        return product.getId();
    }

    @Override
    public String updateProduct(Product product) {
        String sqlLog = "INSERT INTO tbl_productLog(id, name, status) VALUES (?, ?, ?)";
        String sql = "UPDATE tbl_product SET status = ? WHERE id = ?";
        template.update(sqlLog, product.getId(), product.getName(), product.getStatus());
        template.update(sql, product.getStatus(), product.getId());
        return product.getStatus();
    }

    @Override
    public List<Product> findAll(String search) {
        String sql = "SELECT id, name, status FROM tbl_product WHERE id LIKE ? OR name LIKE ?";
        return template.query(sql, ps -> {
            ps.setString(1, search);
            ps.setString(2, search);
        }, new ProductRowMapper());
    }

    @Override
    public Product findById(String id) {
        String sql = "SELECT id, name, status FROM tbl_product WHERE id = ?";
        List<Product> products = template.query(sql, ps -> ps.setString(1, id), new ProductRowMapper());
        return products.isEmpty() ? null : products.get(0);
    }

    private boolean exists(String sql, String param) {
        Integer count = template.queryForObject(sql, Integer.class, param);
        return count != null && count > 0;
    }

    @Override
    public boolean existsById(String id) {
        return exists("SELECT COUNT(*) FROM tbl_product WHERE id = ?", id);
    }

    @Override
    public boolean existsByName(String name) {
        return exists("SELECT COUNT(*) FROM tbl_product WHERE name = ?", name);
    }
}
