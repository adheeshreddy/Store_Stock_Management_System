package com.stockManagement.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stockManagement.models.PurchaseDetails;
import com.stockManagement.models.PurchaseHeader;
import com.stockManagement.service.interfaces.PurchaseService;

@RestController
public class PurchaseController {

    private final PurchaseService purchaseService;
    
    public PurchaseController(PurchaseService purchaseService) {
    	this.purchaseService=purchaseService;
    }

    @PostMapping("/api/purchases")
    public ResponseEntity<Long> createPurchase(@RequestBody PurchaseHeader header) {
        Long created = purchaseService.createPurchase(header);
        return ResponseEntity.ok(created);
    }
    
    @GetMapping("/api/purchases")
    public ResponseEntity<List<PurchaseHeader>> getHeaders() {

        List<PurchaseHeader> headers = purchaseService.getPurchaseHeaders();
        return new ResponseEntity<>(headers,HttpStatus.OK);
    }

    @GetMapping("/api/purchases/details")
    public ResponseEntity<List<PurchaseDetails>> getDetailsByHeaderId(@RequestParam(required = false) String searchTerm) {
        List<PurchaseDetails> details = purchaseService.getDetails(searchTerm);
        return ResponseEntity.ok(details);
    }
    
    @GetMapping("/api/purchases/batches")
    public ResponseEntity<List<PurchaseDetails>> getBatchesByProductId(@RequestParam String productId) {
        List<PurchaseDetails> batches = purchaseService.getBatchesByProductId(productId);
        return ResponseEntity.ok(batches);
    }

    
}
