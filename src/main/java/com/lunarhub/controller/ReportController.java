package com.lunarhub.controller;

import com.lunarhub.service.ReportService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/profit-and-loss")
    public Map<String, Object> profitAndLoss() {
        return reportService.profitAndLoss();
    }

    @GetMapping("/balance-sheet")
    public Map<String, Object> balanceSheet() {
        return reportService.balanceSheet();
    }

    @GetMapping("/revenue-by-module")
    public Map<String, Object> revenueByModule() {
        return reportService.revenueByModule();
    }

    @GetMapping("/budget-variance/{analyticAccountId}")
    public Map<String, Object> budgetVariance(@PathVariable Long analyticAccountId) {
        return reportService.budgetVariance(analyticAccountId);
    }
}
