package com.stockManagement.dao.interfaces.impl;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.stockManagement.dao.interfaces.SupplierDao;
import com.stockManagement.models.Supplier;

@Repository
public class SupplierDaoImpl implements SupplierDao {

	private final JdbcTemplate jdbc;

	public SupplierDaoImpl(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	private final RowMapper<Supplier> mapper = (rs, n) -> {
		Supplier supplier = new Supplier();
		supplier.setId(rs.getLong("id"));
		supplier.setName(rs.getString("name"));
		supplier.setGender(rs.getString("gender"));
		supplier.setMobile(rs.getString("mobile"));
		supplier.setEmail(rs.getString("email"));
		supplier.setCountry(rs.getString("country"));
		supplier.setState(rs.getString("state"));
		supplier.setCity(rs.getString("city"));
		supplier.setAddress(rs.getString("address"));
		supplier.setCreatedBy(rs.getString("createdBy"));
		Timestamp ts = rs.getTimestamp("createdAt");
		if (ts != null) {
			supplier.setCreatedAt(ts.toLocalDateTime());
		}
		return supplier;
	};

	@Override
	public Long saveSupplier(Supplier s) {
		String sql = "INSERT INTO tbl_Supplier(name, gender, mobile, email, country, state, city, address, createdBy) "
				+ "VALUES (?,?,?,?,?,?,?,?,?)";
		KeyHolder kh = new GeneratedKeyHolder();
		jdbc.update(con -> {
			PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, s.getName());
			ps.setString(2, s.getGender());
			ps.setString(3, s.getMobile());
			ps.setString(4, s.getEmail());
			ps.setString(5, s.getCountry());
			ps.setString(6, s.getState());
			ps.setString(7, s.getCity());
			ps.setString(8, s.getAddress());
			ps.setString(9, s.getCreatedBy());
			return ps;
		}, kh);
		s.setId(kh.getKey().longValue());
		return s.getId();
	}

	@Override
	public Long update(Supplier s) {
		String logSql = "INSERT INTO tbl_SupplierLog "
				+ "(supplierId, name, gender, mobile, email, country, state, city, address, createdBy, createdAt, changedBy) "
				+ "SELECT id, name, gender, mobile, email, country, state, city, address, createdBy, createdAt, ? "
				+ "FROM tbl_Supplier WHERE id = ?";

		jdbc.update(logSql, s.getCreatedBy(), s.getId());
		String sql = "UPDATE tbl_Supplier SET name=?, gender=?, mobile=?, email=?, country=?, state=?, city=?, address=? WHERE id=?";
		jdbc.update(sql, s.getName(), s.getGender(), s.getMobile(), s.getEmail(), s.getCountry(), s.getState(),
				s.getCity(), s.getAddress(), s.getId());

		return s.getId();
	}

	@Override
	public Supplier findById(Long id) {
		String sql = "SELECT id,name,gender,mobile,email,country,state,city,address,createdBy,createdAt FROM tbl_Supplier WHERE id=?";
		List<Supplier> list = jdbc.query(sql, mapper, id);
		return list.isEmpty() ? null : list.get(0);
	}

	
	public List<Supplier> findByName(String name) {
		String sql = "SELECT id,name,gender,mobile,email,country,state,city,address,createdBy,createdAt FROM tbl_Supplier WHERE name LIKE ?";
		return jdbc.query(sql, mapper, "%" + name + "%");
	}

	@Override
	public List<Supplier> findAll() {
		return jdbc.query(
				"SELECT id,name,gender,mobile,email,country,state,city,address,createdBy,createdAt FROM tbl_Supplier",
				mapper);
	}

	@Override
	public List<Supplier> findByKey(String key) {
		String sql = "SELECT id,name,gender,mobile,email,country,state,city,address,createdBy,createdAt FROM tbl_Supplier "
				+ "WHERE id = ? OR name LIKE ? ";
		Long idParam = null;
		try {
			idParam = Long.valueOf(key);
		} catch (Exception ignored) {
		}
		return jdbc.query(sql, mapper, idParam == null ? -1L : idParam, "%" + key + "%");
	}

	@Override
	public boolean existsByMobile(String mobile) {
		Integer c = jdbc.queryForObject("SELECT COUNT(*) FROM tbl_Supplier WHERE mobile=?", Integer.class, mobile);
		return c != null && c > 0;
	}

	@Override
	public boolean existsByEmail(String email) {
		Integer c = jdbc.queryForObject("SELECT COUNT(*) FROM tbl_Supplier WHERE email=?", Integer.class, email);
		return c != null && c > 0;
	}
}
