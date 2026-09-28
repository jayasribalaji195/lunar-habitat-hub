package com.lunarhub.controller;

import com.lunarhub.dto.PaymentRequest;
import com.lunarhub.model.Payment;
import com.lunarhub.service.PaymentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /** Collects payment (typically Bank transfer) and posts the Debit Cash / Credit Revenue entry. */
    @PostMapping
    public Payment collect(@RequestBody PaymentRequest request) {
        return paymentService.collectPayment(request);
    }
}
