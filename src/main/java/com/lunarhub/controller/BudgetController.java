package com.lunarhub.controller;

import com.lunarhub.model.AnalyticAccount;
import com.lunarhub.model.Budget;
import com.lunarhub.repository.AnalyticAccountRepository;
import com.lunarhub.repository.BudgetRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budget")
@CrossOrigin(origins = "*")
public class BudgetController {

    private final AnalyticAccountRepository analyticAccountRepository;
    private final BudgetRepository budgetRepository;

    public BudgetController(AnalyticAccountRepository analyticAccountRepository,
                             BudgetRepository budgetRepository) {
        this.analyticAccountRepository = analyticAccountRepository;
        this.budgetRepository = budgetRepository;
    }

    /** Register an Analytic Account, e.g. "Habitat Dome Alpha", tied to a HabitatModule. */
    @PostMapping("/analytic-accounts")
    public AnalyticAccount createAnalyticAccount(@RequestBody AnalyticAccount analyticAccount) {
        return analyticAccountRepository.save(analyticAccount);
    }

    @GetMapping("/analytic-accounts")
    public List<AnalyticAccount> listAnalyticAccounts() {
        return analyticAccountRepository.findAll();
    }

    @PostMapping("/budgets")
    public Budget createBudget(@RequestBody Budget budget) {
        return budgetRepository.save(budget);
    }

    @GetMapping("/budgets")
    public List<Budget> listBudgets() {
        return budgetRepository.findAll();
    }
}
