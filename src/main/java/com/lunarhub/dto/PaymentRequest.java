package com.lunarhub.dto;

import com.lunarhub.model.Payment;

public class PaymentRequest {
    private Long invoiceId;
    private Payment.PaymentMethod method;

    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
    public Payment.PaymentMethod getMethod() { return method; }
    public void setMethod(Payment.PaymentMethod method) { this.method = method; }
}
