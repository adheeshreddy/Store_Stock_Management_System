//package com.stockManagement.service.interfaces.impl;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
//import com.stockManagement.dao.interfaces.UserDao;
//import com.stockManagement.models.User;
//import com.stockManagement.service.interfaces.UserService;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//public class UserServiceImpl implements UserDetailsService, UserService {
//
//	@Autowired
//	private UserDao userDao;
//
//	public UserServiceImpl(UserDao userRepository) {
//		this.userDao = userRepository;
//	}
//
//	@Override
//	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//		User user = userDao.findByEmail(username);
////		System.out.println(user+" from loadbyuser");
//
//		if (user == null) {
//			throw new UsernameNotFoundException("User not found with this email" + username);
//
//		}
//
//		System.out.println("Loaded user: " + user.getEmail() + ", Role: " + user.getRole());
//		List<GrantedAuthority> authorities = new ArrayList<>();
//		return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
//	}
//
//	@Override
//	public List<User> getAllUser() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public User findUserProfileByJwt(String jwt) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public User findUserByEmail(String email) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public User findUserById(String userId) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public List<User> findAllUsers() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//}