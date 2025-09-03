package com.stockManagement.dao.interfaces;

public interface PurchaseLookupDao {
    boolean existsBySupplierAndBatch(Long supplierId, Long batchId);

}
