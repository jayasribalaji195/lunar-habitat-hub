package com.lunarhub.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
public class Invoice {
    public enum InvoiceStatus { UNPAID, PAID }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "sales_order_id")
    private SalesOrder salesOrder;

    /** Quantity billed: m3 of oxygen, liters of water, or service visits — copied from the sales order's product unit. */
    private BigDecimal quantity;

    private BigDecimal amount;
    private BigDecimal tax = BigDecimal.ZERO;
    private BigDecimal total;
    private LocalDateTime createdDate;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status = InvoiceStatus.UNPAID;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SalesOrder getSalesOrder() { return salesOrder; }
    public void setSalesOrder(SalesOrder salesOrder) { this.salesOrder = salesOrder; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }
}
