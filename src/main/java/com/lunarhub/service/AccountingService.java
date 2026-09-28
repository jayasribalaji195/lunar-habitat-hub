package com.lunarhub.service;

import com.lunarhub.model.*;
import com.lunarhub.repository.AccountRepository;
import com.lunarhub.repository.JournalEntryRepository;
import com.lunarhub.repository.JournalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Handles the double-entry bookkeeping side of the system: every billable
 * event posts a balanced JournalEntry (sum of debits == sum of credits).
 */
@Service
public class AccountingService {

    private final AccountRepository accountRepository;
    private final JournalRepository journalRepository;
    private final JournalEntryRepository journalEntryRepository;

    public AccountingService(AccountRepository accountRepository,
                              JournalRepository journalRepository,
                              JournalEntryRepository journalEntryRepository) {
        this.accountRepository = accountRepository;
        this.journalRepository = journalRepository;
        this.journalEntryRepository = journalEntryRepository;
    }

    public Account ensureAccount(String code, String name, Account.AccountType type) {
        return accountRepository.findAll().stream()
                .filter(a -> code.equals(a.getCode()))
                .findFirst()
                .orElseGet(() -> {
                    Account a = new Account();
                    a.setCode(code);
                    a.setName(name);
                    a.setType(type);
                    return accountRepository.save(a);
                });
    }

    public Journal ensureJournal(Journal.JournalType type, String name) {
        return journalRepository.findAll().stream()
                .filter(j -> j.getType() == type)
                .findFirst()
                .orElseGet(() -> {
                    Journal j = new Journal();
                    j.setName(name);
                    j.setType(type);
                    return journalRepository.save(j);
                });
    }

    /** Debit Cash/Bank, Credit Life-Support Utility Revenue (or Habitat Lease Income) upon invoice payment. */
    @Transactional
    public JournalEntry postSalesEntry(Invoice invoice, Product product) {
        Account cash = ensureAccount("CASH_BANK", "Cash/Bank", Account.AccountType.ASSET);
        boolean isLease = product.getName() != null && product.getName().toLowerCase().contains("lease");
        Account revenue = isLease
                ? ensureAccount("LEASE_INC", "Habitat Lease Income", Account.AccountType.INCOME)
                : ensureAccount("UTILITY_REV", "Life-Support Utility Revenue", Account.AccountType.INCOME);
        Journal salesJournal = ensureJournal(Journal.JournalType.SALES, "Tenant Utility Billing");

        JournalEntry entry = new JournalEntry();
        entry.setJournal(salesJournal);
        entry.setDate(LocalDateTime.now());
        entry.setDescription("Bank transfer received for invoice #" + invoice.getId());

        addLine(entry, cash, invoice.getTotal(), BigDecimal.ZERO);
        addLine(entry, revenue, BigDecimal.ZERO, invoice.getTotal());

        return journalEntryRepository.save(entry);
    }

    /**
     * Debit the relevant expense account, Credit the relevant creditor liability, upon vendor bill registration.
     * CO2 Scrubber Servicing (a service) posts to Scrubber Maintenance / Sensor Vendor Creditors;
     * physical goods (e.g. replacement filter cartridges) post to Supply Vessel Logistics / Supply Vessel Payables.
     */
    @Transactional
    public JournalEntry postPurchaseEntry(VendorBill bill, Product product) {
        boolean isService = product.getCategory() == Product.Category.SERVICE;
        Account expense = isService
                ? ensureAccount("SCRUB_EXP", "Scrubber Maintenance Expenditures", Account.AccountType.EXPENSE)
                : ensureAccount("LOGISTICS_EXP", "Supply Vessel Logistics Expenses", Account.AccountType.EXPENSE);
        Account creditors = isService
                ? ensureAccount("SENSOR_CRED", "Sensor Vendor Creditors", Account.AccountType.LIABILITY)
                : ensureAccount("VESSEL_PAY", "Supply Vessel Payables", Account.AccountType.LIABILITY);
        Journal purchaseJournal = ensureJournal(Journal.JournalType.PURCHASE, "Facility Procurement");

        JournalEntry entry = new JournalEntry();
        entry.setJournal(purchaseJournal);
        entry.setDate(LocalDateTime.now());
        entry.setDescription("Vendor bill #" + bill.getId());

        addLine(entry, expense, bill.getAmount(), BigDecimal.ZERO);
        addLine(entry, creditors, BigDecimal.ZERO, bill.getAmount());

        return journalEntryRepository.save(entry);
    }

    /** Debit the creditor liability, Credit Cash/Bank, when a vendor bill is paid. */
    @Transactional
    public JournalEntry postBillPayment(VendorBill bill, Product product) {
        boolean isService = product.getCategory() == Product.Category.SERVICE;
        Account creditors = isService
                ? ensureAccount("SENSOR_CRED", "Sensor Vendor Creditors", Account.AccountType.LIABILITY)
                : ensureAccount("VESSEL_PAY", "Supply Vessel Payables", Account.AccountType.LIABILITY);
        Account cash = ensureAccount("CASH_BANK", "Cash/Bank", Account.AccountType.ASSET);
        Journal bankJournal = ensureJournal(Journal.JournalType.BANK, "Bank Payments");

        JournalEntry entry = new JournalEntry();
        entry.setJournal(bankJournal);
        entry.setDate(LocalDateTime.now());
        entry.setDescription("Payment for vendor bill #" + bill.getId());

        addLine(entry, creditors, bill.getAmount(), BigDecimal.ZERO);
        addLine(entry, cash, BigDecimal.ZERO, bill.getAmount());

        return journalEntryRepository.save(entry);
    }

    private void addLine(JournalEntry entry, Account account, BigDecimal debit, BigDecimal credit) {
        JournalEntryLine line = new JournalEntryLine();
        line.setJournalEntry(entry);
        line.setAccount(account);
        line.setDebit(debit);
        line.setCredit(credit);
        entry.getLines().add(line);
    }
}
