package com.ebanx.challenge.model;

public class Account {

    private final String id;
    private Integer balance;

    public Account(String id, Integer balance) {
        this.id = id;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public Integer getBalance() {
        return balance;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }
}