package com.stockManagement.service.interfaces;

import java.util.List;

import com.stockManagement.models.User;

public interface UserService {

	public List<User> getAllUser();

	public User findUserProfileByJwt(String jwt);

	public User findUserByEmail(String email);

	public User findUserById(String userId);

	public List<User> findAllUsers();

}