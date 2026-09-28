package com.lunarhub.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {
    public enum PoStatus { DRAFT, CONFIRMED, BILLED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vendor_id")
    private Contact vendor;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer quantity = 1;
    private BigDecimal unitPrice;
    private LocalDateTime createdDate;

    @Enumerated(EnumType.STRING)
    private PoStatus status = PoStatus.DRAFT;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Contact getVendor() { return vendor; }
    public void setVendor(Contact vendor) { this.vendor = vendor; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public PoStatus getStatus() { return status; }
    public void setStatus(PoStatus status) { this.status = status; }
}
