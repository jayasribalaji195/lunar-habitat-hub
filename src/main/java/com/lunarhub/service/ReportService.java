package com.lunarhub.service;

import com.lunarhub.model.*;
import com.lunarhub.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final JournalEntryLineRepository lineRepository;
    private final AccountRepository accountRepository;
    private final InvoiceRepository invoiceRepository;
    private final HabitatModuleRepository moduleRepository;
    private final AnalyticAccountRepository analyticAccountRepository;
    private final BudgetRepository budgetRepository;

    public ReportService(JournalEntryLineRepository lineRepository,
                          AccountRepository accountRepository,
                          InvoiceRepository invoiceRepository,
                          HabitatModuleRepository moduleRepository,
                          AnalyticAccountRepository analyticAccountRepository,
                          BudgetRepository budgetRepository) {
        this.lineRepository = lineRepository;
        this.accountRepository = accountRepository;
        this.invoiceRepository = invoiceRepository;
        this.moduleRepository = moduleRepository;
        this.analyticAccountRepository = analyticAccountRepository;
        this.budgetRepository = budgetRepository;
    }

    /** Profit & Loss: utility + lease revenues minus scrubber maintenance & logistics expenses. */
    public Map<String, Object> profitAndLoss() {
        List<Account> incomeAccounts = accountRepository.findAll().stream()
                .filter(a -> a.getType() == Account.AccountType.INCOME).collect(Collectors.toList());
        List<Account> expenseAccounts = accountRepository.findAll().stream()
                .filter(a -> a.getType() == Account.AccountType.EXPENSE).collect(Collectors.toList());

        Map<String, BigDecimal> incomeByAccount = new LinkedHashMap<>();
        BigDecimal totalIncome = BigDecimal.ZERO;
        for (Account a : incomeAccounts) {
            BigDecimal net = netCreditMinusDebit(a.getId());
            incomeByAccount.put(a.getName(), net);
            totalIncome = totalIncome.add(net);
        }

        Map<String, BigDecimal> expenseByAccount = new LinkedHashMap<>();
        BigDecimal totalExpense = BigDecimal.ZERO;
        for (Account a : expenseAccounts) {
            BigDecimal net = netDebitMinusCredit(a.getId());
            expenseByAccount.put(a.getName(), net);
            totalExpense = totalExpense.add(net);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("income", incomeByAccount);
        result.put("totalIncome", totalIncome);
        result.put("expenses", expenseByAccount);
        result.put("totalExpense", totalExpense);
        result.put("netProfit", totalIncome.subtract(totalExpense));
        return result;
    }

    /** Balance Sheet: habitat dome assets vs. open supply liabilities. */
    public Map<String, Object> balanceSheet() {
        List<Account> assetAccounts = accountRepository.findAll().stream()
                .filter(a -> a.getType() == Account.AccountType.ASSET).collect(Collectors.toList());
        List<Account> liabilityAccounts = accountRepository.findAll().stream()
                .filter(a -> a.getType() == Account.AccountType.LIABILITY).collect(Collectors.toList());

        Map<String, BigDecimal> assets = new LinkedHashMap<>();
        BigDecimal totalAssets = BigDecimal.ZERO;
        for (Account a : assetAccounts) {
            BigDecimal net = netDebitMinusCredit(a.getId());
            assets.put(a.getName(), net);
            totalAssets = totalAssets.add(net);
        }

        Map<String, BigDecimal> liabilities = new LinkedHashMap<>();
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        for (Account a : liabilityAccounts) {
            BigDecimal net = netCreditMinusDebit(a.getId());
            liabilities.put(a.getName(), net);
            totalLiabilities = totalLiabilities.add(net);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("assets", assets);
        result.put("totalAssets", totalAssets);
        result.put("liabilities", liabilities);
        result.put("totalLiabilities", totalLiabilities);
        result.put("equity", totalAssets.subtract(totalLiabilities));
        return result;
    }

    /** Revenue collected per habitat module (paid invoices only). */
    public Map<String, Object> revenueByModule() {
        Map<String, Object> byModule = new LinkedHashMap<>();
        for (HabitatModule module : moduleRepository.findAll()) {
            BigDecimal total = invoiceRepository.findAll().stream()
                    .filter(inv -> inv.getStatus() == Invoice.InvoiceStatus.PAID)
                    .filter(inv -> inv.getSalesOrder() != null && inv.getSalesOrder().getModule() != null
                            && inv.getSalesOrder().getModule().getId().equals(module.getId()))
                    .map(Invoice::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            byModule.put(module.getName(), total);
        }
        return byModule;
    }

    /** Budget Report: planned life-support budget vs. actual utility revenue, per analytic account (facility zone). */
    public Map<String, Object> budgetVariance(Long analyticAccountId) {
        AnalyticAccount analyticAccount = analyticAccountRepository.findById(analyticAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Analytic account not found: " + analyticAccountId));

        BigDecimal planned = budgetRepository.findByAnalyticAccountId(analyticAccountId).stream()
                .map(Budget::getPlannedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal actual = BigDecimal.ZERO;
        if (analyticAccount.getModule() != null) {
            actual = invoiceRepository.findAll().stream()
                    .filter(inv -> inv.getStatus() == Invoice.InvoiceStatus.PAID)
                    .filter(inv -> inv.getSalesOrder() != null && inv.getSalesOrder().getModule() != null
                            && inv.getSalesOrder().getModule().getId().equals(analyticAccount.getModule().getId()))
                    .map(Invoice::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("analyticAccount", analyticAccount.getName());
        result.put("plannedAmount", planned);
        result.put("actualAmount", actual);
        result.put("variance", actual.subtract(planned));
        return result;
    }

    private BigDecimal netDebitMinusCredit(Long accountId) {
        return lineRepository.findByAccountId(accountId).stream()
                .map(l -> l.getDebit().subtract(l.getCredit()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal netCreditMinusDebit(Long accountId) {
        return lineRepository.findByAccountId(accountId).stream()
                .map(l -> l.getCredit().subtract(l.getDebit()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
