package com.lunarhub.controller;

import com.lunarhub.dto.CreateSalesOrderRequest;
import com.lunarhub.model.Invoice;
import com.lunarhub.model.SalesOrder;
import com.lunarhub.repository.SalesOrderRepository;
import com.lunarhub.service.LifeSupportOrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lunar/sales-orders")
@CrossOrigin(origins = "*")
public class SalesOrderController {

    private final LifeSupportOrderService orderService;
    private final SalesOrderRepository salesOrderRepository;

    public SalesOrderController(LifeSupportOrderService orderService, SalesOrderRepository salesOrderRepository) {
        this.orderService = orderService;
        this.salesOrderRepository = salesOrderRepository;
    }

    @PostMapping
    public SalesOrder create(@RequestBody CreateSalesOrderRequest request) {
        return orderService.createSalesOrder(request);
    }

    @GetMapping
    public List<SalesOrder> list() {
        return salesOrderRepository.findAll();
    }

    /** Converts consumption details (from telemetry) into a Customer Invoice. */
    @PostMapping("/{id}/invoice")
    public Invoice invoice(@PathVariable Long id) {
        return orderService.convertToInvoice(id);
    }
}
