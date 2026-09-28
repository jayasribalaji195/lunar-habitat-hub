package com.lunarhub.controller;

import com.lunarhub.model.PurchaseOrder;
import com.lunarhub.model.VendorBill;
import com.lunarhub.repository.PurchaseOrderRepository;
import com.lunarhub.repository.VendorBillRepository;
import com.lunarhub.service.AccountingService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/purchase")
@CrossOrigin(origins = "*")
public class PurchaseController {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final VendorBillRepository vendorBillRepository;
    private final AccountingService accountingService;

    public PurchaseController(PurchaseOrderRepository purchaseOrderRepository,
                               VendorBillRepository vendorBillRepository,
                               AccountingService accountingService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.vendorBillRepository = vendorBillRepository;
        this.accountingService = accountingService;
    }

    /** Create a Purchase Order, e.g. for replacement chemical CO2 filter cartridges. */
    @PostMapping("/orders")
    public PurchaseOrder createOrder(@RequestBody PurchaseOrder order) {
        order.setCreatedDate(LocalDateTime.now());
        order.setStatus(PurchaseOrder.PoStatus.CONFIRMED);
        return purchaseOrderRepository.save(order);
    }

    @GetMapping("/orders")
    public List<PurchaseOrder> listOrders() {
        return purchaseOrderRepository.findAll();
    }

    /** Convert a confirmed PO into a Vendor Bill and post the appropriate expense/creditor journal entry. */
    @PostMapping("/orders/{id}/bill")
    public VendorBill createBillFromOrder(@PathVariable Long id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Purchase order not found: " + id));

        BigDecimal amount = order.getUnitPrice().multiply(BigDecimal.valueOf(order.getQuantity()));

        VendorBill bill = new VendorBill();
        bill.setPurchaseOrder(order);
        bill.setAmount(amount);
        bill.setDueDate(LocalDateTime.now().plusDays(15));
        bill.setStatus(VendorBill.BillStatus.UNPAID);
        bill = vendorBillRepository.save(bill);

        order.setStatus(PurchaseOrder.PoStatus.BILLED);
        purchaseOrderRepository.save(order);

        accountingService.postPurchaseEntry(bill, order.getProduct());
        return bill;
    }

    /** Pay a vendor bill via Bank and post the settlement journal entry. */
    @PutMapping("/bills/{id}/pay")
    public VendorBill payBill(@PathVariable Long id) {
        VendorBill bill = vendorBillRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vendor bill not found: " + id));
        if (bill.getStatus() == VendorBill.BillStatus.PAID) {
            throw new IllegalStateException("Bill already paid");
        }
        bill.setStatus(VendorBill.BillStatus.PAID);
        bill = vendorBillRepository.save(bill);
        accountingService.postBillPayment(bill, bill.getPurchaseOrder().getProduct());
        return bill;
    }

    @GetMapping("/bills")
    public List<VendorBill> listBills() {
        return vendorBillRepository.findAll();
    }
}
