package com.ebanx.challenge.service;

import com.ebanx.challenge.dto.EventRequest;
import com.ebanx.challenge.model.Account;
import com.ebanx.challenge.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Map;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository repository;

    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }

    public Integer getBalance(String accountId) {
        log.debug("Getting balance for account: {}", accountId);
        return repository.findById(accountId)
                .map(Account::getBalance)
                .orElseThrow();
    }

    public Object processEvent(EventRequest request) {
        log.info("Processing event: type={}, origin={}, destination={}, amount={}",
                request.type(), request.origin(), request.destination(), request.amount());
        validateAmount(request.amount());

        return switch (request.type()) {
            case "deposit" -> deposit(request);
            case "withdraw" -> withdraw(request);
            case "transfer" -> transfer(request);
            default -> throw new IllegalArgumentException("Invalid event type: " + request.type());
        };
    }

    public void reset() {
        log.info("Resetting all accounts state");
        repository.deleteAll();
    }

    private void validateAmount(Integer amount) {
        if (amount == null || amount <= 0) {
            log.warn("Invalid amount: {}", amount);
            throw new IllegalArgumentException("Amount must be a positive, non-zero value");
        }
    }

    private void validateSufficientFunds(Account account, Integer amount) {
        if (account.getBalance() < amount) {
            log.warn("Insufficient funds for account: {}, balance: {}, requested: {}",
                    account.getId(), account.getBalance(), amount);
            throw new IllegalArgumentException("Insufficient funds for account: " + account.getId());
        }
    }

    private Object deposit(EventRequest request) {
        extracted(request.destination(), "Null or blank destination for deposit", "Destination account ID is required for deposit");

        var account = repository.findById(request.destination())
                .orElse(new Account(request.destination(), 0));

        account.setBalance(account.getBalance() + request.amount());
        repository.save(account);

        log.info("Deposit successful: account={}, newBalance={}", account.getId(), account.getBalance());
        return getStringMapMap(account);
    }

    private static Map<String, Map<String, ? extends Serializable>> getStringMapMap(Account account) {
        return Map.of(
                "destination",
                Map.of(
                        "id", account.getId(),
                        "balance", account.getBalance()
                )
        );
    }

    private Object withdraw(EventRequest request) {
        extracted(request.origin(), "Null or blank origin for withdrawal", "Origin account ID is required for withdrawal");

        var account = repository.findById(request.origin())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + request.origin()));

        validateSufficientFunds(account, request.amount());

        account.setBalance(account.getBalance() - request.amount());
        repository.save(account);

        log.info("Withdrawal successful: account={}, newBalance={}", account.getId(), account.getBalance());
        return Map.of(
                "origin",
                Map.of(
                        "id", account.getId(),
                        "balance", account.getBalance()
                )
        );
    }

    private Object transfer(EventRequest request) {
        extracted(request.origin(), "Null or blank origin for transfer", "Origin account ID is required for transfer");

        extracted(request.destination(), "Null or blank destination for transfer", "Destination account ID is required for transfer");

        var origin = repository.findById(request.origin())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + request.origin()));

        var destination = repository.findById(request.destination())
                .orElse(new Account(request.destination(), 0));

        validateSufficientFunds(origin, request.amount());

        origin.setBalance(origin.getBalance() - request.amount());
        destination.setBalance(destination.getBalance() + request.amount());

        repository.save(origin);
        repository.save(destination);

        log.info("Transfer successful: origin={}, destination={}, amount={}",
                origin.getId(), destination.getId(), request.amount());
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

    private static void extracted(String request, String s, String s1) {
        if (request == null || request.isBlank()) {
            log.warn(s);
            throw new IllegalArgumentException(s1);
        }
    }
}