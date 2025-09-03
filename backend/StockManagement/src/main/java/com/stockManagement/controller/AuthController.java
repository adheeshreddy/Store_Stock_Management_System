package com.stockManagement.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stockManagement.dao.interfaces.UserDao;
import com.stockManagement.models.User;
import com.stockManagement.security.JwtUtil;

@RestController
@RequestMapping("/auth")
public class AuthController {
	private final UserDao userDao;
	private final JwtUtil jwtUtil;

	public AuthController(UserDao userDao, JwtUtil jwtUtil) {
		this.userDao = userDao;
		this.jwtUtil = jwtUtil;
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
		String username = request.get("username");
		String password = request.get("password");
		System.out.println(username);
		System.out.println(password);
		User user = userDao.findByUsername(username);

		if (user == null || !user.getPassword().equals(password)) {
			return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
		}

		String token = jwtUtil.generateToken(username);
		return ResponseEntity.ok(Map.of("token", token, "username", username));
	}
}