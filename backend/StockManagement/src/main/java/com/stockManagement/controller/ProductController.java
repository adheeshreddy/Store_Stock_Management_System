package com.stockManagement.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stockManagement.models.Product;
import com.stockManagement.service.interfaces.ProductService;

@RestController
public class ProductController {

    private final ProductService productService;
    
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/api/product")
    public ResponseEntity<String> addProduct(@RequestBody Product product) {
        return new ResponseEntity<>(productService.addProduct(product), HttpStatus.CREATED);
    }

    @PostMapping("/api/product/add")
    public ResponseEntity<List<String>> addProductList(@RequestBody List<Product> products) {
        return new ResponseEntity<>(productService.addProducts(products), HttpStatus.CREATED);
    }

    @GetMapping("/api/product")
    public ResponseEntity<List<Product>> getProducts(@RequestParam(required = false) String product) {
        return new ResponseEntity<>(productService.getAllProducts(product), HttpStatus.OK);
    }

    @PutMapping("/api/product/{id}")
    public ResponseEntity<String> updateProduct(@RequestBody String status, @PathVariable String id) {
        String updated = productService.updateProduct(status, id);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }
}