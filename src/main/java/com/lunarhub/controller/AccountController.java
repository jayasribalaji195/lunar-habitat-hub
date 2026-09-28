package com.lunarhub.controller;

import com.lunarhub.model.Account;
import com.lunarhub.repository.AccountRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "*")
public class AccountController {

    private final AccountRepository accountRepository;

    public AccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @PostMapping
    public Account create(@RequestBody Account account) {
        return accountRepository.save(account);
    }

    @GetMapping
    public List<Account> list() {
        return accountRepository.findAll();
    }
}
