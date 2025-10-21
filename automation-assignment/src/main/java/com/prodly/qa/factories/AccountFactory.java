package com.prodly.qa.factories;

import com.prodly.qa.config.Config;
import com.prodly.qa.models.Account;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class AccountFactory {
    private static final String[] COUNTRIES = {"US", "GB", "DE", "FR", "UA"};

    public static List<Account> build(int count) {
        List<Account> accs = new ArrayList<>();
        Set<String> unique = new HashSet<>();
        for (int i = 0; i < count; i++) {
            Account a = buildSingle(i);
            String key = a.getName() + "|" + a.getCountry();
            if (!unique.add(key)) throw new AssertionError("Duplicate Name+Country: " + key);
            if ("US".equalsIgnoreCase(a.getCountry()) &&
                    (a.getNumberOfEmployees() == null || a.getNumberOfEmployees() <= 100)) {
                throw new AssertionError("US account must have NumberOfEmployees > 100: " + a.getName());
            }
            accs.add(a);
        }
        return accs;
    }

    private static Account buildSingle(int idx) {
        String country = COUNTRIES[idx % COUNTRIES.length];
        int employees = "US".equals(country)
                ? ThreadLocalRandom.current().nextInt(150, 500)
                : ThreadLocalRandom.current().nextInt(20, 120);
        String name = "AutoAcct-" + Config.RUN_ID + "-" + UUID.randomUUID().toString().substring(0,8);
        return new AccountBuilder().name(name).country(country).employees(employees).build();
    }
}
