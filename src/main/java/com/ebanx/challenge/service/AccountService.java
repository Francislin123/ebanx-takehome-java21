package com.ebanx.challenge.service;

import com.ebanx.challenge.dto.EventRequest;
import com.ebanx.challenge.model.Account;
import com.ebanx.challenge.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AccountService {

    private final AccountRepository repository;

    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }

    public Integer getBalance(String accountId) {
        return repository.findById(accountId)
                .map(Account::getBalance)
                .orElseThrow();
    }

    public Object processEvent(EventRequest request) {
        validateAmount(request.amount());

        return switch (request.type()) {
            case "deposit" -> deposit(request);
            case "withdraw" -> withdraw(request);
            case "transfer" -> transfer(request);
            default -> throw new IllegalArgumentException("Invalid event type: " + request.type());
        };
    }

    private void validateAmount(Integer amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be a positive, non-zero value");
        }
    }

    private void validateSufficientFunds(Account account, Integer amount) {
        if (account.getBalance() < amount) {
            throw new IllegalArgumentException("Insufficient funds for account: " + account.getId());
        }
    }

    private Object deposit(EventRequest request) {
        if (request.destination() == null || request.destination().isBlank()) {
            throw new IllegalArgumentException("Destination account ID is required for deposit");
        }

        var account = repository.findById(request.destination())
                .orElse(new Account(request.destination(), 0));

        account.setBalance(account.getBalance() + request.amount());

        repository.save(account);

        return Map.of(
                "destination",
                Map.of(
                        "id", account.getId(),
                        "balance", account.getBalance()
                )
        );
    }

    private Object withdraw(EventRequest request) {
        if (request.origin() == null || request.origin().isBlank()) {
            throw new IllegalArgumentException("Origin account ID is required for withdrawal");
        }

        var account = repository.findById(request.origin())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + request.origin()));

        validateSufficientFunds(account, request.amount());

        account.setBalance(account.getBalance() - request.amount());

        repository.save(account);

        return Map.of(
                "origin",
                Map.of(
                        "id", account.getId(),
                        "balance", account.getBalance()
                )
        );
    }

    private Object transfer(EventRequest request) {
        if (request.origin() == null || request.origin().isBlank()) {
            throw new IllegalArgumentException("Origin account ID is required for transfer");
        }

        if (request.destination() == null || request.destination().isBlank()) {
            throw new IllegalArgumentException("Destination account ID is required for transfer");
        }

        var origin = repository.findById(request.origin())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + request.origin()));

        var destination = repository.findById(request.destination())
                .orElse(new Account(request.destination(), 0));

        validateSufficientFunds(origin, request.amount());

        origin.setBalance(origin.getBalance() - request.amount());
        destination.setBalance(destination.getBalance() + request.amount());

        repository.save(origin);
        repository.save(destination);

        return Map.of(
                "origin",
                Map.of(
                        "id", origin.getId(),
                        "balance", origin.getBalance()
                ),
                "destination",
                Map.of(
                        "id", destination.getId(),
                        "balance", destination.getBalance()
                )
        );
    }
}