package com.stockManagement.validation;

import com.stockManagement.exception.InvalidDataException;

public class ValidationUtil {
	
	 private ValidationUtil() {}

	    public static void requireNotNull(Object value, String field) {
	        if (value == null) throw new InvalidDataException(field + " is required");
	    }

	    public static void requireNotBlank(String value, String field) {
	        if (value == null || value.isBlank())
	            throw new InvalidDataException(field + " is required");
	    }

	    public static void requirePositive(int value, String field) {
	        if (value <= 0 ) throw new InvalidDataException(field + " must be > 0");
	    }

	    public static void requirePositive(double value, String field) {
	        if (value < 0) throw new InvalidDataException(field + " cannot be negative");
	    }
}
