package com.stockManagement.models;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Supplier {
	
	 private Long id;   

	    @NotBlank(message = "Supplier name is required")
	    @Size(max = 30, message = "Name must not exceed 30 characters")
	    private String name;

	    @NotBlank(message = "Gender is required")
	    @Pattern(regexp = "M|F", message = "Gender must be either 'M' or 'F'")
	    private String gender;

	    @NotBlank(message = "Mobile number is required")
	    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be 10 digits")
	    private String mobile;

	    @NotBlank(message = "Email is required")
	    @Email(message = "Invalid email format")
	    @Size(max = 40, message = "Email must not exceed 40 characters")
	    private String email;

	    @NotBlank(message = "Country is required")
	    @Size(max = 20, message = "Country must not exceed 20 characters")
	    private String country;

	    @NotBlank(message = "State is required")
	    @Size(max = 20, message = "State must not exceed 20 characters")
	    private String state;

	    @NotBlank(message = "City is required")
	    @Size(max = 20, message = "City must not exceed 20 characters")
	    private String city;

	    @NotBlank(message = "Address is required")
	    @Size(max = 255, message = "Address must not exceed 255 characters")
	    private String address;

	    @NotBlank(message = "CreatedBy is required")
	    @Size(max = 30, message = "CreatedBy must not exceed 30 characters")
	    private String createdBy;

	    
	    private LocalDateTime createdAt;
}
