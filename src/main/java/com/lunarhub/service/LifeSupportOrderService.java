package com.lunarhub.service;

import com.lunarhub.dto.CreateModuleRequest;
import com.lunarhub.dto.CreateSalesOrderRequest;
import com.lunarhub.model.*;
import com.lunarhub.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Handles habitat module registration and the Sales Order -> Customer Invoice
 * flow, pulling billable consumption straight from the telemetry log.
 */
@Service
public class LifeSupportOrderService {

    private final HabitatModuleRepository moduleRepository;
    private final ContactRepository contactRepository;
    private final ProductRepository productRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final InvoiceRepository invoiceRepository;
    private final TelemetryReadingRepository telemetryRepository;

    public LifeSupportOrderService(HabitatModuleRepository moduleRepository,
                                    ContactRepository contactRepository,
                                    ProductRepository productRepository,
                                    SalesOrderRepository salesOrderRepository,
                                    InvoiceRepository invoiceRepository,
                                    TelemetryReadingRepository telemetryRepository) {
        this.moduleRepository = moduleRepository;
        this.contactRepository = contactRepository;
        this.productRepository = productRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.invoiceRepository = invoiceRepository;
        this.telemetryRepository = telemetryRepository;
    }

    /** POST /api/lunar/modules — registers a habitat module with its nominal life-support thresholds. */
    @Transactional
    public HabitatModule createModule(CreateModuleRequest request) {
        HabitatModule module = new HabitatModule();
        module.setName(request.getName());
        module.setZone(request.getZone());
        module.setDomeAreaM2(request.getDomeAreaM2());
        module.setPressureMinKPa(request.getPressureMinKPa());
        module.setPressureMaxKPa(request.getPressureMaxKPa());
        module.setWaterPurityMinPct(request.getWaterPurityMinPct());
        module.setStatus(HabitatModule.ModuleStatus.NOMINAL);
        return moduleRepository.save(module);
    }

    public List<HabitatModule> listModules() {
        return moduleRepository.findAll();
    }

    /** Issues a monthly life-support service order to a commercial tenant for a given module/product/period. */
    @Transactional
    public SalesOrder createSalesOrder(CreateSalesOrderRequest request) {
        Contact tenant = contactRepository.findById(request.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + request.getTenantId()));
        HabitatModule module = moduleRepository.findById(request.getModuleId())
                .orElseThrow(() -> new IllegalArgumentException("Habitat module not found: " + request.getModuleId()));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + request.getProductId()));

        SalesOrder order = new SalesOrder();
        order.setTenant(tenant);
        order.setModule(module);
        order.setProduct(product);
        order.setPeriodStart(request.getPeriodStart());
        order.setPeriodEnd(request.getPeriodEnd());
        order.setStatus(SalesOrder.OrderStatus.CONFIRMED);
        return salesOrderRepository.save(order);
    }

    /**
     * Converts a confirmed Sales Order into a Customer Invoice. For goods (Reclaimed Oxygen,
     * Potable Water) the billable quantity is summed straight from telemetry consumption over
     * the order's period; for services (Scrubber Servicing, Habitat Lease) it's billed flat.
     */
    @Transactional
    public Invoice convertToInvoice(Long salesOrderId) {
        SalesOrder order = salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Sales order not found: " + salesOrderId));
        if (order.getStatus() == SalesOrder.OrderStatus.INVOICED) {
            throw new IllegalStateException("Sales order already invoiced");
        }

        Product product = order.getProduct();
        BigDecimal quantity;

        if (product.getCategory() == Product.Category.GOODS) {
            LocalDateTime start = order.getPeriodStart().atStartOfDay();
            LocalDateTime end = order.getPeriodEnd().atTime(LocalTime.MAX);
            List<TelemetryReading> readings = telemetryRepository
                    .findByModuleIdAndTimestampBetween(order.getModule().getId(), start, end);

            String name = product.getName() == null ? "" : product.getName().toLowerCase();
            if (name.contains("water")) {
                quantity = readings.stream().map(TelemetryReading::getWaterConsumedLiters)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            } else {
                quantity = readings.stream().map(TelemetryReading::getOxygenConsumedM3)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
        } else {
            quantity = BigDecimal.ONE; // flat per-visit / per-period service charge
        }

        BigDecimal amount = product.getPrice().multiply(quantity).setScale(2, RoundingMode.HALF_UP);

        Invoice invoice = new Invoice();
        invoice.setSalesOrder(order);
        invoice.setQuantity(quantity);
        invoice.setAmount(amount);
        invoice.setTax(BigDecimal.ZERO);
        invoice.setTotal(amount);
        invoice.setCreatedDate(LocalDateTime.now());
        invoice.setStatus(Invoice.InvoiceStatus.UNPAID);
        invoice = invoiceRepository.save(invoice);

        order.setStatus(SalesOrder.OrderStatus.INVOICED);
        salesOrderRepository.save(order);

        return invoice;
    }
}
