package com.prodly.qa.factories;

import com.prodly.qa.models.Account;

public class AccountBuilder {
    private final Account a = new Account();
    public AccountBuilder name(String n){ a.setName(n); return this; }
    public AccountBuilder country(String c){ a.setCountry(c); return this; }
    public AccountBuilder employees(int e){ a.setNumberOfEmployees(e); return this; }
    public Account build(){ return a; }
}
