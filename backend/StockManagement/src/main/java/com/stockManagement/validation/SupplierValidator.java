package com.stockManagement.validation;

import com.stockManagement.exception.InvalidDataException;
import com.stockManagement.models.Supplier;
import com.stockManagement.utilities.StringUtil;

public final class SupplierValidator {

	

	public static void validateForCreate(Supplier s) {
		if (s == null)
			throw new InvalidDataException("Supplier body is required");
		req("name", s.getName());
		req("gender", s.getGender());
		req("mobile", s.getMobile());
		req("email", s.getEmail());
		req("country", s.getCountry());
		req("state", s.getState());
		req("city", s.getCity());
		req("address", s.getAddress());
		req("createdBy", s.getCreatedBy());
		req("createdAt", String.valueOf(s.getCreatedAt()));

		if (!"M".equals(s.getGender()) && !"F".equals(s.getGender())) {
			throw new InvalidDataException("Gender must be M or F");
		}
		if (!s.getMobile().matches("^[0-9]{10}$")) {
			throw new InvalidDataException("Mobile number must be 10 digits");
		}
		if (!s.getEmail().contains("@")) {
			throw new InvalidDataException("Invalid email format");
		}
		
	}

	public static void validateForUpdate(Supplier s) {
		if (s == null)
			throw new InvalidDataException("Supplier body is required");
		if (StringUtil.isBlank(s.getId()))
			throw new InvalidDataException("id is required for update");
		req("name", s.getName());
		req("gender", s.getGender());
		req("mobile", s.getMobile());
		req("email", s.getEmail());
		req("country", s.getCountry());
		req("state", s.getState());
		req("city", s.getCity());
		req("address", s.getAddress());
		req("createdBy", s.getCreatedBy());
		req("createdAt", String.valueOf(s.getCreatedAt()));
		
		
		
		if (!"M".equals(s.getGender()) && !"F".equals(s.getGender())) {
			throw new InvalidDataException("Gender must be M or F");
		}
		if (!s.getEmail().contains("@")) {
			throw new InvalidDataException("Invalid email format");
		}
		if (!s.getMobile().matches("^[0-9]{10}$")) {
			throw new InvalidDataException("Mobile number must be 10 digits");
		}
		
	}

	private static void req(String field, String value) {
		if (StringUtil.isBlank(value)) {
			throw new InvalidDataException("Supplier's "+field + " is required");
		}
	}
}
