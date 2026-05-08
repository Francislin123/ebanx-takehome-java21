package com.ebanx.challenge.controller;

import com.ebanx.challenge.dto.EventRequest;
import com.ebanx.challenge.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AccountController {

    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @GetMapping("/balance")
    public ResponseEntity<?> balance(@RequestParam("account_id") String accountId) {
        return ResponseEntity.ok(service.getBalance(accountId));
    }

    @PostMapping("/event")
    public ResponseEntity<?> event(@RequestBody EventRequest request) {
        return ResponseEntity.status(201).body(service.processEvent(request));
    }
}