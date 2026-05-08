package com.ebanx.challenge.repository;

import com.ebanx.challenge.model.Account;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AccountRepository {

    private final ConcurrentHashMap<String, Account> accounts = new ConcurrentHashMap<>();

    public Optional<Account> findById(String id) {
        return Optional.ofNullable(accounts.get(id));
    }

    public Account save(Account account) {
        accounts.put(account.getId(), account);
        return account;
    }

    public void deleteAll() {
        accounts.clear();
    }
}