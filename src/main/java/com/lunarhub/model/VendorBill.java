package com.lunarhub.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vendor_bills")
public class VendorBill {
    public enum BillStatus { UNPAID, PAID }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "purchase_order_id")
    private PurchaseOrder purchaseOrder;

    private BigDecimal amount;
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    private BillStatus status = BillStatus.UNPAID;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PurchaseOrder getPurchaseOrder() { return purchaseOrder; }
    public void setPurchaseOrder(PurchaseOrder purchaseOrder) { this.purchaseOrder = purchaseOrder; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    public BillStatus getStatus() { return status; }
    public void setStatus(BillStatus status) { this.status = status; }
}
