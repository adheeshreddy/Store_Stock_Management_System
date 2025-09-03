package com.stockManagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.stockManagement")
public class StockManagementApplication {
	public static void main(String[] args) {
		SpringApplication.run(StockManagementApplication.class, args);
	}
}
