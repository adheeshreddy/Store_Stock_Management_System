package com.stockManagement.dao.interfaces;

import java.util.List;

import com.stockManagement.models.ProformaHeader;

public interface ProformaDao {
    public Long insertProforma(ProformaHeader header);                
    public Long updateProforma(ProformaHeader header);                
    public Long softDelete(Long headerId, String byUser);     
    public ProformaHeader approveProforma(Long headerId);    
    public ProformaHeader findById(Long id, boolean withDetails);
    public List<ProformaHeader> findAll(boolean withDetails);
}
