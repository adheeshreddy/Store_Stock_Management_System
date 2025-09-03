package com.stockManagement.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stockManagement.models.StockOutDetails;
import com.stockManagement.models.StockOutHeader;
import com.stockManagement.service.interfaces.StockOutService;

import jakarta.validation.Valid;


@Validated
@RestController
public class StockOutController {

    private final StockOutService stockOutService;

    public StockOutController(StockOutService stockOutService) {
        this.stockOutService = stockOutService;
    }

    @PostMapping("/api/stockout")
    public ResponseEntity<StockOutHeader> createStockOut(@Valid @RequestBody StockOutHeader header) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockOutService.createStockOut(header));
    }

    @GetMapping("/api/stockout")
    public ResponseEntity<List<StockOutHeader>> getHeaders() {
        return ResponseEntity.ok(stockOutService.getStockOutHeaders());
    }

    @GetMapping("/api/stockout/{id}/details")
    public ResponseEntity<List<StockOutDetails>> getDetailsByHeaderId(@PathVariable Long id) {
        return ResponseEntity.ok(stockOutService.getDetailsByStockId(id));
    }
    
    @GetMapping("/api/stockout/supplier/{id}")
    public ResponseEntity<List<StockOutHeader>> getHeadersBySupplier(@PathVariable Long id) {
        return ResponseEntity.ok(stockOutService.getHeadersBySupplierId(id));
    }
    
    @GetMapping("/api/stockout/suppliers/{id}")
    public ResponseEntity<List<StockOutHeader>> getHeadersByProformaHeader(@PathVariable Long id) {
        return ResponseEntity.ok(stockOutService.getHeadersByProformaId(id));
    }
}
