package com.stockManagement.service.interfaces;

import java.util.List;

import com.stockManagement.models.ProformaHeader;

public interface ProformaService {
	
	public Long createProforma(ProformaHeader header);
	public Long editProforma(ProformaHeader header);
	public Long deleteProforma(ProformaHeader header);
	public ProformaHeader approveProforma(ProformaHeader header);
	public List<ProformaHeader> getAllProformas( Boolean details);
	public ProformaHeader getProformaById(Long id, boolean details);
	
}
