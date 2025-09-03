package com.stockManagement.service.interfaces.impl;

import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stockManagement.dao.interfaces.SupplierDao;
import com.stockManagement.exception.ResourceAlreadyExistsException;
import com.stockManagement.exception.ResourceNotFoundException;
import com.stockManagement.models.Supplier;
import com.stockManagement.service.interfaces.SupplierService;
import com.stockManagement.utilities.StringUtil;
import com.stockManagement.validation.SupplierValidator;

@Service
public class SupplierServiceImpl implements SupplierService {

	private final SupplierDao supplierDao;

	public SupplierServiceImpl(SupplierDao supplierDao) {
		this.supplierDao = supplierDao;
	}

	@Override
	@Transactional
	public Long addSupplier(Supplier supplier) {
		SupplierValidator.validateForCreate(supplier);
		if (supplierDao.existsByMobile(supplier.getMobile())) {
			throw new ResourceAlreadyExistsException("Mobile already exists: " + supplier.getMobile());
		}
		if (supplierDao.existsByEmail(supplier.getEmail())) {
			throw new ResourceAlreadyExistsException("Email already exists: " + supplier.getEmail());
		}
		return supplierDao.saveSupplier(supplier);
	}

	@Override
	@Transactional
	public Long updateSupplier(Long id, Supplier supplier) {
		SupplierValidator.validateForUpdate(supplier);
		Supplier existing = supplierDao.findById(supplier.getId());
		if (existing == null) {
			throw new ResourceNotFoundException("Supplier not found with id: " + supplier.getId());
		}
		
		if (supplierDao.existsByMobile(supplier.getMobile())) {
			throw new ResourceAlreadyExistsException("Mobile already exists: " + supplier.getMobile());
		}
		if (supplierDao.existsByEmail(supplier.getEmail())) {
			throw new ResourceAlreadyExistsException("Email already exists: " + supplier.getEmail());
		}
		
		existing.setName(supplier.getName());
		existing.setGender(supplier.getGender());
		existing.setMobile(supplier.getMobile());
		existing.setEmail(supplier.getEmail());
		existing.setCountry(supplier.getCountry());
		existing.setState(supplier.getState());
		existing.setCity(supplier.getCity());
		existing.setAddress(supplier.getAddress());
		existing.setCreatedBy(supplier.getCreatedBy());

		return supplierDao.update(existing);
	}

	@Override
	@Transactional
	public List<Supplier> getSuppliers(String searchKey) {
		if (StringUtil.isBlank(searchKey)) {
			return supplierDao.findAll();
		}
		return supplierDao.findByKey(searchKey);
	}

	@Override
	@Transactional
	public String getSupplierById(Long id) {
		Supplier supplier = supplierDao.findById(id);
		return supplier.getName();
	}

	@Override
	@Transactional
	public Map<Long, String> getAllSuppliers() {

		List<Supplier> supplierObjects = supplierDao.findAll();
		Map<Long, String> suppliers = supplierObjects.stream().collect(toMap(Supplier::getId, Supplier::getName));

		return suppliers;

	}
}
