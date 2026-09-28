package com.lunarhub.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "sales_orders")
public class SalesOrder {
    public enum OrderStatus { CONFIRMED, INVOICED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tenant_id")
    private Contact tenant;

    @ManyToOne
    @JoinColumn(name = "module_id")
    private HabitatModule module;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private LocalDate periodStart;
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.CONFIRMED;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Contact getTenant() { return tenant; }
    public void setTenant(Contact tenant) { this.tenant = tenant; }
    public HabitatModule getModule() { return module; }
    public void setModule(HabitatModule module) { this.module = module; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}
