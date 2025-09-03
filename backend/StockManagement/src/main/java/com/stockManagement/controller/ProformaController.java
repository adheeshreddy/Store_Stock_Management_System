package com.stockManagement.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stockManagement.models.ProformaHeader;
import com.stockManagement.service.interfaces.ProformaService;

@RestController
@RequestMapping("/api/proformas")
public class ProformaController {

    private final ProformaService service;

    public ProformaController(ProformaService service) { this.service = service; }

    @PostMapping
    public Long create(@RequestBody ProformaHeader header) {
        return service.createProforma(header);
    }

    @PutMapping
    public Long edit(@RequestBody ProformaHeader header) {
        return service.editProforma(header);
    }

    @DeleteMapping("/{id}")
    public Long delete(@PathVariable Long id, @RequestParam(required = false) String by) {
        ProformaHeader h = new ProformaHeader();
        h.setId(id);
        h.setCreatedBy(by != null ? by : "system");
        return service.deleteProforma(h);
    }

    @PutMapping("/{id}/approve")
    public Long approve(@PathVariable Long id, @RequestParam(required = false) String by) {
    	System.out.println(id+" "+by);
        ProformaHeader h = new ProformaHeader();
        h.setId(id);
        h.setCreatedBy(by != null ? by : "manager");
        System.out.println(h);
        return service.approveProforma(h).getId();
    }

    @GetMapping
    public List<ProformaHeader> list(@RequestParam(defaultValue = "true") boolean details) {
        return service.getAllProformas( details);
    }

    @GetMapping("/{id}")
    public ProformaHeader getOne(@PathVariable Long id,
                                 @RequestParam(defaultValue = "true") boolean details) {
        return service.getProformaById(id, details);
    }


}