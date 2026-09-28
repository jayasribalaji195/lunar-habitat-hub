package com.lunarhub.controller;

import com.lunarhub.model.Invoice;
import com.lunarhub.repository.InvoiceRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@CrossOrigin(origins = "*")
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;

    public InvoiceController(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @GetMapping
    public List<Invoice> list() {
        return invoiceRepository.findAll();
    }

    @GetMapping("/{id}")
    public Invoice get(@PathVariable Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + id));
    }
}
