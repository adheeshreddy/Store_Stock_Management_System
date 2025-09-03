package com.stockManagement.dao.interfaces;

import com.stockManagement.models.User;

public interface UserDao {
	
	public User findByUsername(String username);
}
