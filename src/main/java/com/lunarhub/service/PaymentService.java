package com.lunarhub.service;

import com.lunarhub.dto.PaymentRequest;
import com.lunarhub.model.Invoice;
import com.lunarhub.model.Payment;
import com.lunarhub.repository.InvoiceRepository;
import com.lunarhub.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final AccountingService accountingService;

    public PaymentService(InvoiceRepository invoiceRepository,
                           PaymentRepository paymentRepository,
                           AccountingService accountingService) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.accountingService = accountingService;
    }

    /** Collects payment (typically Bank transfer) for a tenant invoice and posts the Cash/Revenue journal entry. */
    @Transactional
    public Payment collectPayment(PaymentRequest request) {
        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + request.getInvoiceId()));

        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            throw new IllegalStateException("Invoice already paid");
        }

        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmount(invoice.getTotal());
        payment.setMethod(request.getMethod());
        payment.setDate(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        invoice.setStatus(Invoice.InvoiceStatus.PAID);
        invoiceRepository.save(invoice);

        accountingService.postSalesEntry(invoice, invoice.getSalesOrder().getProduct());

        return payment;
    }
}
