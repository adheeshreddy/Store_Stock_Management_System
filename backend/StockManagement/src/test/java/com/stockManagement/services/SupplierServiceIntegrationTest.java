package com.stockManagement.services;

import com.stockManagement.exception.ResourceAlreadyExistsException;
import com.stockManagement.exception.ResourceNotFoundException;
import com.stockManagement.models.Supplier;
import com.stockManagement.service.interfaces.SupplierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SupplierServiceIntegrationTest {

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long existingSupplierId;
    private int initialSupplierCount;
    private int initialLogCount;

    private Supplier createSampleSupplier(String name, String mobile, String email) {
        return new Supplier(null, name, "F", mobile, email,
                "India", "KA", "Mysore", "Some address", "tester", LocalDateTime.now());
    }

    private int countSuppliers() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tbl_Supplier", Integer.class);
    }

    private int countSupplierLogs() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tbl_SupplierLog", Integer.class);
    }

    @BeforeEach
    void setup() {
        initialSupplierCount = countSuppliers();
        System.out.println(initialSupplierCount);
        initialLogCount = countSupplierLogs();

        Supplier baseSupplier = createSampleSupplier("Base Supplier", "1111111111", "base@mail.com");
        existingSupplierId = supplierService.addSupplier(baseSupplier);
        System.out.println(countSuppliers());
    }


    @Test
    void testAddSupplier_Success() {
        Supplier newSupplier = createSampleSupplier("Alice", "9999999999", "alice@test.com");
        Long newId = supplierService.addSupplier(newSupplier);

        assertNotNull(newId);
        assertNotEquals(existingSupplierId, newId);
        
        assertEquals(initialSupplierCount + 2, countSuppliers());
    }

    @Test
    void testAddSupplier_DuplicateMobile_RollsBackTransaction() {
        Supplier duplicateMobileSupplier = createSampleSupplier("Alice2", "1111111111", "b@b.com");

        assertThrows(ResourceAlreadyExistsException.class,
                () -> supplierService.addSupplier(duplicateMobileSupplier));

        assertEquals(initialSupplierCount + 1, countSuppliers());
    }


    @Test
    void testUpdateSupplier_Success_DataAndLogUpdated() {
        Supplier updates = new Supplier();
        updates.setId(existingSupplierId);
        updates.setAddress("Updated Address");
        updates.setCreatedBy("updater"); 

        Long updatedId = supplierService.updateSupplier(existingSupplierId, updates);

        assertEquals(existingSupplierId, updatedId);

        Supplier updatedSupplier = supplierService.getSuppliers(null).stream()
                .filter(s -> s.getId().equals(existingSupplierId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Supplier not found"));

        assertEquals("Updated Address", updatedSupplier.getAddress());

        assertEquals(initialLogCount + 1, countSupplierLogs());
        
        Map<String, Object> logEntry = jdbcTemplate.queryForMap("SELECT * FROM tbl_SupplierLog WHERE supplierId = ?", existingSupplierId);
        assertEquals("Base Supplier", logEntry.get("name"));
        assertEquals("updater", logEntry.get("changedBy"));
    }
    
    @Test
    void testUpdateSupplier_NotFound() {
        Supplier s = createSampleSupplier("Fake", "4444444444", "fake@mail.com");
        s.setId(999L);

        assertThrows(ResourceNotFoundException.class,
                () -> supplierService.updateSupplier(999L, s));
        
        assertEquals(initialSupplierCount + 1, countSuppliers());
        assertEquals(initialLogCount, countSupplierLogs());
    }
    
    @Test
    void testUpdateSupplier_MobileConflict_TransactionRollsBack() {
        Supplier s2 = createSampleSupplier("Second Supplier", "2222222222", "second@mail.com");
        supplierService.addSupplier(s2);
        
        Supplier updates = new Supplier();
        updates.setId(existingSupplierId);
        updates.setMobile("2222222222"); 
        
        assertThrows(ResourceAlreadyExistsException.class,
                () -> supplierService.updateSupplier(updates.getId(), updates));

        Supplier unchangedSupplier = supplierService.getSuppliers(null).stream()
                .filter(s -> s.getId().equals(existingSupplierId))
                .findFirst().orElseThrow();
                
        assertEquals("1111111111", unchangedSupplier.getMobile());
        
        assertEquals(initialSupplierCount + 2, countSuppliers());
        assertEquals(initialLogCount, countSupplierLogs());
    }
    
    @Test
    void testUpdateSupplier_EmailConflict_TransactionRollsBack() {
        Supplier s2 = createSampleSupplier("Second Supplier", "2222222222", "second@mail.com");
        supplierService.addSupplier(s2);
        
        Supplier updates = new Supplier();
        updates.setId(existingSupplierId);
        updates.setEmail("second@mail.com"); 
        
        assertThrows(ResourceAlreadyExistsException.class,
                () -> supplierService.updateSupplier(updates.getId(), updates));

        Supplier unchangedSupplier = supplierService.getSuppliers(null).stream()
                .filter(s -> s.getId().equals(existingSupplierId))
                .findFirst().orElseThrow();
                
        assertEquals("base@mail.com", unchangedSupplier.getEmail());
        
        assertEquals(initialSupplierCount + 2, countSuppliers());
        assertEquals(initialLogCount, countSupplierLogs());
    }
}