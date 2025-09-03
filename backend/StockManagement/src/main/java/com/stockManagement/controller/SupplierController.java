package com.stockManagement.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stockManagement.models.Supplier;
import com.stockManagement.service.interfaces.SupplierService;

import jakarta.validation.Valid;

@RestController
public class SupplierController {
	
	private final SupplierService supplierService;
	
	public  SupplierController(SupplierService supplierService) {
		this.supplierService=supplierService;
	}

	@PostMapping("/api/suppliers")
	public Long createSupplier(@Valid @RequestBody Supplier supplier) {
		return supplierService.addSupplier(supplier);
	}

	@GetMapping("/api/suppliers")
	public List<Supplier> getSuppliers(@RequestParam(required = false) String searchKey) {
		return supplierService.getSuppliers(searchKey);
	}
	
	@GetMapping("/api/suppliers/all")
	public Map<Long,String> getAllSuppliers(){
		return supplierService.getAllSuppliers();
	}
	
	
	@GetMapping("/api/suppliers/{id}")
	public String getSupplierById(@PathVariable Long id) {
	    return supplierService.getSupplierById(id);
	}


	@PutMapping("/api/suppliers/{id}")
	public Long updateSupplier(@PathVariable Long id, @Valid @RequestBody Supplier supplier) {
		return supplierService.updateSupplier(id, supplier);
	}
}
